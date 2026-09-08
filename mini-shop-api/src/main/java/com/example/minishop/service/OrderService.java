package com.example.minishop.service;

import com.example.minishop.constant.*;
import com.example.minishop.dto.request.CreateOrderItemRequest;
import com.example.minishop.dto.request.CreateOrderRequest;
import com.example.minishop.dto.request.UpdateOrderStatusRequest;
import com.example.minishop.dto.response.BestVoucherResult;
import com.example.minishop.dto.response.CustomerShopOrderResponse;
import com.example.minishop.dto.response.OrderResponse;
import com.example.minishop.dto.response.VoucherApplicationResult;
import com.example.minishop.entity.*;
import com.example.minishop.event.ProductStockChangedEvent;
import com.example.minishop.event.payload.OrderOutboxPayload;
import com.example.minishop.event.payload.ShopOrderOutboxPayload;
import com.example.minishop.exception.BadRequestException;
import com.example.minishop.exception.ResourceNotFoundException;
import com.example.minishop.mapper.CustomerShopOrderMapper;
import com.example.minishop.mapper.OrderMapper;
import com.example.minishop.repository.OrderRepository;
import com.example.minishop.repository.ShopOrderRepository;
import com.example.minishop.security.SecurityUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final OrderMapper orderMapper;
    private final AddressService addressService;
    private final VoucherService voucherService;
    private final UserVoucherService userVoucherService;
    private final PricingService pricingService;
    private final StockService stockService;
    private final OutboxService outboxService;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderCancellationService orderCancellationService;
    private final ShopOrderCodeGenerator shopOrderCodeGenerator;
    private final ShopOrderRepository shopOrderRepository;
    private final CustomerShopOrderMapper customerShopOrderMapper;
    private final FlashSalePurchaseService flashSalePurchaseService;

    public OrderService(
            OrderRepository orderRepository,
            ProductService productService,
            OrderMapper orderMapper,
            AddressService addressService,
            VoucherService voucherService,
            UserVoucherService userVoucherService,
            PricingService pricingService,
            StockService stockService,
            OutboxService outboxService,
            ApplicationEventPublisher eventPublisher,
            OrderCancellationService orderCancellationService,
            ShopOrderCodeGenerator shopOrderCodeGenerator,
            ShopOrderRepository shopOrderRepository,
            CustomerShopOrderMapper customerShopOrderMapper,
            FlashSalePurchaseService flashSalePurchaseService
    ) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.orderMapper = orderMapper;
        this.addressService = addressService;
        this.voucherService = voucherService;
        this.userVoucherService = userVoucherService;
        this.pricingService = pricingService;
        this.stockService = stockService;
        this.outboxService = outboxService;
        this.eventPublisher = eventPublisher;
        this.orderCancellationService = orderCancellationService;
        this.shopOrderCodeGenerator = shopOrderCodeGenerator;
        this.shopOrderRepository = shopOrderRepository;
        this.customerShopOrderMapper = customerShopOrderMapper;
        this.flashSalePurchaseService = flashSalePurchaseService;
    }

    private Order getEntityById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy đơn hàng với id: " + id
                ));
    }

    private record OrderItemKey(
            Long productId,
            Long variantId
    ) {}

    private record AddOrderItemsResult(
            BigDecimal totalAmount,
            Set<Long> productIds
    ) {
    }

    private Order getMyOrderEntityById(Long orderId) {

        Long userId = SecurityUtils.getCurrentUserId();

        return orderRepository
                .findByIdAndUser_Id(orderId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy đơn hàng"
                        )
                );
    }

    private boolean hasVoucher(String voucherCode) {
        return voucherCode != null && !voucherCode.isBlank();
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException(
                    "Đơn hàng phải có ít nhất một sản phẩm"
            );
        }

        User user = getCurrentActiveUser();
        Order order = buildOrder(user, request);
        order.setAddress(getShippingAddress(request));
        order = orderRepository.saveAndFlush(order);
        AddOrderItemsResult itemsResult = addOrderItems(order, request.getItems());
        BigDecimal totalAmount = itemsResult.totalAmount();
        order.setTotalAmount(itemsResult.totalAmount());
        String voucherCode = request.getVoucherCode();

        if (!hasVoucher(voucherCode)
                && Boolean.TRUE.equals(request.getAutoApplyBestVoucher())) {

            BestVoucherResult bestVoucher = userVoucherService.findBestVoucher( user.getId(), order);
            voucherCode = bestVoucher.getVoucherCode();
        }

        if (hasVoucher(voucherCode)) {
            userVoucherService.markUsed(
                    user.getId(),
                    voucherCode
            );
        }

        VoucherApplicationResult voucherResult =
                voucherService.applyToOrder(voucherCode, order);
        applyVoucherResult(order, voucherResult);
        Order savedOrder =
                orderRepository.save(order);
        outboxService.addEvent(
                OutboxEventType.ORDER_CREATED,
                "ORDER",
                savedOrder.getId(),
                new OrderOutboxPayload(savedOrder.getId())
        );

        eventPublisher.publishEvent(
                new ProductStockChangedEvent(
                        itemsResult.productIds()
                )
        );

        return orderMapper.toResponse(savedOrder);
    }

    private void applyVoucherResult(
            Order order,
            VoucherApplicationResult result
    ) {
        BigDecimal discount = result.getDiscountAmount();

        order.setVoucherCode(result.getVoucherCode());
        order.setDiscountAmount(discount);
        order.setFinalAmount(
                order.getTotalAmount().subtract(discount)
        );

        if (result.getScope() == null) {
            allocateOrderDiscount(order, BigDecimal.ZERO);
            return;
        }

        if (result.getScope() == VoucherScope.PLATFORM) {
            allocateOrderDiscount(order, discount);
            return;
        }

        applyShopVoucher(
                order,
                result.getShopId(),
                discount
        );
    }

    private void applyShopVoucher(
            Order order,
            Long shopId,
            BigDecimal discount
    ) {
        for (ShopOrder shopOrder : order.getShopOrders()) {
            BigDecimal shopDiscount = BigDecimal.ZERO;

            if (shopOrder.getShop().getId().equals(shopId)) {
                shopDiscount = discount;
            }

            shopOrder.setDiscountAmount(shopDiscount);

            shopOrder.setFinalAmount(
                    shopOrder.getSubtotal()
                            .add(shopOrder.getShippingFee())
                            .subtract(shopDiscount)
            );
        }
    }

    private User getCurrentActiveUser() {

        User user = SecurityUtils
                .getCurrentUser()
                .getUser();

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new BadRequestException(
                    "Tài khoản đã bị khóa"
            );
        }

        return user;
    }

    private Order buildOrder(User user, CreateOrderRequest request) {
        Order order = new Order();

        order.setUser(user);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        if (request.getPaymentMethod() == PaymentMethod.VNPAY) {
            order.setStatus(OrderStatus.WAITING_PAYMENT);
        } else {
            order.setStatus(OrderStatus.PENDING);
        }

        return order;
    }

    private AddOrderItemsResult addOrderItems(
            Order order,
            List<CreateOrderItemRequest> requests
    ) {
        BigDecimal orderTotal = BigDecimal.ZERO;

        Map<Long, ShopOrder> shopOrderMap = new HashMap<>();
        Set<Long> productIds = new HashSet<>();
        Set<OrderItemKey> itemKeys = new HashSet<>();

        for (CreateOrderItemRequest request : requests) {
            OrderItemKey itemKey = new OrderItemKey(
                    request.getProductId(),
                    request.getVariantId()
            );

            if (!itemKeys.add(itemKey)) {
                throw new BadRequestException(
                        "Sản phẩm/biến thể bị lặp trong đơn hàng"
                );
            }

            productIds.add(request.getProductId());
            Product product = productService.getProductEntityById(
                    request.getProductId()
            );
            ProductVariant variant = null;

            if (request.getVariantId() != null) {

                variant = product.getVariants()
                        .stream()
                        .filter(v ->
                                v.getId().equals(request.getVariantId())
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy biến thể sản phẩm"
                                )
                        );

            } else if (product.getVariants() != null
                    && !product.getVariants().isEmpty()) {

                throw new BadRequestException(
                        "Vui lòng chọn đầy đủ biến thể sản phẩm"
                );
            }

            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new BadRequestException(
                        "Sản phẩm hiện không được phép bán: " + product.getName()
                );
            }

            if (product.getShop().getStatus() != ShopStatus.ACTIVE) {
                throw new BadRequestException(
                        "Shop của sản phẩm hiện không hoạt động"
                );
            }

            OrderItem item = createOrderItem(product, variant, request);

            stockService.decreaseStock(
                    product,
                    variant,
                    request.getQuantity(),
                    order
            );

            Shop shop = product.getShop();
            ShopOrder shopOrder =
                    shopOrderMap.get(shop.getId());

            if (shopOrder == null) {

                shopOrder =
                        createShopOrder(
                                shop,
                                order
                        );

                order.addShopOrder(shopOrder);
                shopOrder = shopOrderRepository.saveAndFlush(shopOrder);
                shopOrderMap.put(shop.getId(), shopOrder);
            }
            order.addItem(item);
            shopOrder.addItem(item);
            shopOrder.setSubtotal(
                    shopOrder.getSubtotal().add(item.getSubtotal())
            );
            orderTotal = orderTotal.add(item.getSubtotal());
        }

        for (ShopOrder shopOrder : shopOrderMap.values()) {
            shopOrder.setFinalAmount(shopOrder.getSubtotal());
        }

        return new AddOrderItemsResult(
                orderTotal,
                Set.copyOf(productIds)
        );
    }

    private void allocateOrderDiscount(
            Order order,
            BigDecimal totalDiscount
    ) {
        BigDecimal discount = totalDiscount == null
                ? BigDecimal.ZERO
                : totalDiscount;

        BigDecimal orderSubtotal = order.getTotalAmount();

        if (orderSubtotal == null
                || orderSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal allocated = BigDecimal.ZERO;
        List<ShopOrder> shopOrders = order.getShopOrders();

        for (int index = 0; index < shopOrders.size(); index++) {
            ShopOrder shopOrder = shopOrders.get(index);

            BigDecimal shopDiscount = BigDecimal.ZERO;

            if (discount.compareTo(BigDecimal.ZERO) > 0) {
                if (index == shopOrders.size() - 1) {
                    shopDiscount = discount.subtract(allocated);
                } else {
                    shopDiscount = discount
                            .multiply(shopOrder.getSubtotal())
                            .divide(
                                    orderSubtotal,
                                    2,
                                    RoundingMode.HALF_UP
                            );

                    allocated = allocated.add(shopDiscount);
                }
            }

            shopOrder.setDiscountAmount(shopDiscount);

            shopOrder.setFinalAmount(
                    shopOrder.getSubtotal()
                            .add(shopOrder.getShippingFee())
                            .subtract(shopDiscount)
            );
        }
    }

    private ShopOrder createShopOrder(
            Shop shop,
            Order order
    ) {

        ShopOrder shopOrder =
                new ShopOrder();

        shopOrder.setOrderCode(
                shopOrderCodeGenerator.generate()
        );

        shopOrder.setShop(shop);

        shopOrder.setStatus(
                ShopOrderStatus.PENDING
        );

        shopOrder.setPaymentMethod(
                order.getPaymentMethod()
        );

        shopOrder.setPaymentStatus(
                PaymentStatus.PENDING
        );

        shopOrder.setSubtotal(
                BigDecimal.ZERO
        );

        shopOrder.setDiscountAmount(
                BigDecimal.ZERO
        );

        shopOrder.setShippingFee(
                BigDecimal.ZERO
        );

        shopOrder.setFinalAmount(
                BigDecimal.ZERO
        );

        shopOrder.setCreatedAt(
                LocalDateTime.now()
        );

        return shopOrder;
    }

    private OrderItem createOrderItem(
            Product product,
            ProductVariant variant,
            CreateOrderItemRequest request
    ) {
        BigDecimal normalPrice =
                variant != null
                        ? variant.getPrice()
                        : product.getPrice();

        FlashSale flashSale =
                flashSalePurchaseService
                        .findActiveFlashSale(
                                product
                        );

        BigDecimal currentPrice =
                normalPrice;

        if (flashSale != null) {
            FlashSale consumedFlashSale =
                    flashSalePurchaseService
                            .consume(
                                    flashSale.getId(),
                                    request.getQuantity()
                            );

            currentPrice =
                    consumedFlashSale
                            .getSalePrice();

            flashSale =
                    consumedFlashSale;
        }

        BigDecimal subtotal =
                currentPrice.multiply(
                        BigDecimal.valueOf(
                                request.getQuantity()
                        )
                );

        OrderItem item =
                new OrderItem();

        item.setProduct(product);
        item.setVariant(variant);
        item.setFlashSale(flashSale);
        item.setQuantity(
                request.getQuantity()
        );
        item.setPrice(currentPrice);
        item.setSubtotal(subtotal);

        return item;
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long id){

        return orderMapper.toResponse(getMyOrderEntityById(id));

    }

    @Transactional
    public CustomerShopOrderResponse cancelOrder(
            String orderCode
    ) {

        ShopOrder shopOrder =
                getMyShopOrderByCode(orderCode);

        if (shopOrder.getStatus()
                != ShopOrderStatus.PENDING
                && shopOrder.getStatus()
                != ShopOrderStatus.CONFIRMED) {

            throw new BadRequestException(
                    "Không thể hủy đơn hàng " +
                            "ở trạng thái hiện tại"
            );
        }

        if (shopOrder.getPaymentMethod()
                == PaymentMethod.VNPAY) {

            throw new BadRequestException(
                    "Đơn thanh toán VNPay hiện chưa hỗ trợ " +
                            "hủy riêng theo shop"
            );
        }

        for (OrderItem item :
                shopOrder.getItems()) {

            stockService.restoreStock(
                    item.getProduct(),
                    item.getVariant(),
                    item.getQuantity(),
                    shopOrder.getOrder()
            );

            if (item.getFlashSale() != null) {
                flashSalePurchaseService.restore(
                        item.getFlashSale().getId(),
                        item.getQuantity()
                );
            }
        }

        shopOrder.setStatus(
                ShopOrderStatus.CANCELLED
        );

        shopOrder.setCancelledAt(
                LocalDateTime.now()
        );

        shopOrder.setCancelReason(
                "Khách hàng hủy đơn"
        );

        updateParentOrderStatus(
                shopOrder.getOrder()
        );

        outboxService.addEvent(
                OutboxEventType.ORDER_CANCELLED,
                "SHOP_ORDER",
                shopOrder.getId(),
                new ShopOrderOutboxPayload(
                        shopOrder.getId()
                )
        );

        return customerShopOrderMapper
                .toResponse(shopOrder);
    }

    @Transactional
    public OrderResponse updateStatus(
            Long id,
            UpdateOrderStatusRequest request
    ) {
        Order order = getEntityById(id);

        if (request.getStatus() != OrderStatus.CANCELLED) {
            throw new BadRequestException(
                    "Trạng thái đơn tổng được tự động đồng bộ từ các đơn của shop"
            );
        }
        orderCancellationService.cancel(order);

        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {

        Long userId = SecurityUtils.getCurrentUserId();

        return orderRepository.findByUser_Id(userId)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerShopOrderResponse>
    getMyShopOrders() {

        Long userId =
                SecurityUtils.getCurrentUserId();

        return shopOrderRepository
                .findByOrder_User_IdOrderByCreatedAtDesc(
                        userId
                )
                .stream()
                .map(
                        customerShopOrderMapper::toResponse
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerShopOrderResponse
    getMyShopOrderByCodeResponse(
            String orderCode
    ) {

        return customerShopOrderMapper
                .toResponse(
                        getMyShopOrderByCode(
                                orderCode
                        )
                );
    }

    @Transactional
    public CustomerShopOrderResponse confirmReceived(
            String orderCode
    ) {

        ShopOrder shopOrder =
                getMyShopOrderByCode(orderCode);

        if (shopOrder.getStatus()
                != ShopOrderStatus.DELIVERED) {

            throw new BadRequestException(
                    "Đơn hàng chưa được giao, " +
                            "không thể xác nhận đã nhận hàng"
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        shopOrder.setStatus(
                ShopOrderStatus.COMPLETED
        );

        shopOrder.setCompletedAt(now);

        if (shopOrder.getPaymentMethod()
                == PaymentMethod.COD
                && shopOrder.getPaymentStatus()
                == PaymentStatus.PENDING) {

            shopOrder.setPaymentStatus(
                    PaymentStatus.PAID
            );

            shopOrder.setPaidAt(now);
        }

        updateParentOrderStatus(
                shopOrder.getOrder()
        );

        updateParentOrderPaymentStatus(
                shopOrder.getOrder(),
                now
        );

        outboxService.addEvent(
                OutboxEventType.ORDER_RECEIVED,
                "SHOP_ORDER",
                shopOrder.getId(),
                new ShopOrderOutboxPayload(
                        shopOrder.getId()
                )
        );

        return customerShopOrderMapper
                .toResponse(shopOrder);
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new BadRequestException(
                    "Page không được nhỏ hơn 0"
            );
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException(
                    "Size phải nằm trong khoảng 1 đến 100"
            );
        }
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAll(int page, int size) {
        validatePagination(page, size);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        return orderRepository.findAll(pageable)
                .map(orderMapper::toResponse);
    }

    private Address getShippingAddress(CreateOrderRequest request) {
        if (request.getAddressId() != null) {
            return addressService.getMyAddressEntity(request.getAddressId());
        }

        return addressService.getMyDefaultAddressEntity();
    }

    private ShopOrder getMyShopOrderByCode(
            String orderCode
    ) {

        Long userId =
                SecurityUtils.getCurrentUserId();

        return shopOrderRepository
                .findByOrderCodeAndOrder_User_Id(
                        orderCode,
                        userId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy đơn hàng"
                        )
                );
    }

    private void updateParentOrderPaymentStatus(
            Order order,
            LocalDateTime now
    ) {

        if (order.getPaymentMethod()
                != PaymentMethod.COD) {
            return;
        }

        List<ShopOrder> activeShopOrders =
                order.getShopOrders()
                        .stream()
                        .filter(shopOrder ->
                                shopOrder.getStatus()
                                        != ShopOrderStatus.CANCELLED
                        )
                        .toList();

        if (activeShopOrders.isEmpty()) {
            return;
        }

        boolean allPaid =
                activeShopOrders
                        .stream()
                        .allMatch(shopOrder ->
                                shopOrder.getPaymentStatus()
                                        == PaymentStatus.PAID
                        );

        if (allPaid) {
            order.setPaymentStatus(
                    PaymentStatus.PAID
            );

            order.setPaidAt(now);
        }
    }

    private void updateParentOrderStatus(
            Order order
    ) {

        boolean allCancelled =
                order.getShopOrders()
                        .stream()
                        .allMatch(shopOrder ->
                                shopOrder.getStatus()
                                        == ShopOrderStatus.CANCELLED
                        );

        if (allCancelled) {
            order.setStatus(
                    OrderStatus.CANCELLED
            );
            return;
        }

        boolean allFinished =
                order.getShopOrders()
                        .stream()
                        .allMatch(shopOrder ->
                                shopOrder.getStatus()
                                        == ShopOrderStatus.COMPLETED
                                        ||
                                        shopOrder.getStatus()
                                                == ShopOrderStatus.CANCELLED
                        );

        if (allFinished) {
            order.setStatus(
                    OrderStatus.COMPLETED
            );
            return;
        }

        boolean anyShipping =
                order.getShopOrders()
                        .stream()
                        .anyMatch(shopOrder ->
                                shopOrder.getStatus()
                                        == ShopOrderStatus.SHIPPING
                        );

        if (anyShipping) {
            order.setStatus(
                    OrderStatus.SHIPPING
            );
            return;
        }

        boolean anyDelivered =
                order.getShopOrders()
                        .stream()
                        .anyMatch(shopOrder ->
                                shopOrder.getStatus()
                                        == ShopOrderStatus.DELIVERED
                        );

        if (anyDelivered) {
            order.setStatus(
                    OrderStatus.DELIVERED
            );
            return;
        }

        boolean nonePending =
                order.getShopOrders()
                        .stream()
                        .noneMatch(shopOrder ->
                                shopOrder.getStatus()
                                        == ShopOrderStatus.PENDING
                        );

        if (nonePending) {
            order.setStatus(
                    OrderStatus.CONFIRMED
            );
        }
    }

}