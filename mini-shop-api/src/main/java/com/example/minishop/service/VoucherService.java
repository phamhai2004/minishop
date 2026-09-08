package com.example.minishop.service;

import com.example.minishop.constant.DiscountType;
import com.example.minishop.constant.VoucherScope;
import com.example.minishop.dto.request.VoucherRequest;
import com.example.minishop.dto.response.VoucherApplicationResult;
import com.example.minishop.dto.response.VoucherResponse;
import com.example.minishop.entity.Order;
import com.example.minishop.entity.Shop;
import com.example.minishop.entity.ShopOrder;
import com.example.minishop.entity.Voucher;
import com.example.minishop.exception.BadRequestException;
import com.example.minishop.exception.ResourceNotFoundException;
import com.example.minishop.mapper.VoucherMapper;
import com.example.minishop.repository.VoucherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherMapper voucherMapper;

    public VoucherService(
            VoucherRepository voucherRepository,
            VoucherMapper voucherMapper
    ) {
        this.voucherRepository = voucherRepository;
        this.voucherMapper = voucherMapper;
    }

    @Transactional
    public VoucherResponse create(
            VoucherRequest request
    ) {
        String code = request.getCode().trim().toUpperCase();

        if (
                voucherRepository.existsByCodeIgnoreCase(code)
        ) {
            throw new BadRequestException(
                    "Mã voucher đã tồn tại"
            );
        }

        validateRequest(request);
        Voucher voucher = voucherMapper.toEntity(request);
        voucher.setScope(VoucherScope.PLATFORM);
        voucher.setShop(null);

        if (request.getActive() == null) {
            voucher.setActive(true);
        }

        return voucherMapper.toResponse(voucherRepository.save(voucher));
    }

    @Transactional(readOnly = true)
    public List<VoucherResponse> getAll() {

        return voucherRepository
                .findByScopeOrderByIdDesc(
                        VoucherScope.PLATFORM
                )
                .stream()
                .map(voucherMapper::toResponse)
                .toList();
    }

    @Transactional
    public VoucherResponse update(
            Long id,
            VoucherRequest request
    ) {

        Voucher voucher =
                getPlatformVoucherForUpdate(id);

        validateRequest(request);

        String code =
                request.getCode()
                        .trim()
                        .toUpperCase();

        boolean alreadyCollected =
                voucherRepository
                        .hasBeenCollected(
                                voucher.getId()
                        );

        if (
                alreadyCollected
                        && hasIssuedConditionChanged(
                        voucher,
                        request,
                        code
                )
        ) {
            throw new BadRequestException(
                    "Voucher đã có khách hàng nhận, không thể thay đổi điều kiện đã phát hành"
            );
        }

        if (
                !voucher.getCode()
                        .equalsIgnoreCase(code)
                        && voucherRepository
                        .existsByCodeIgnoreCase(code)
        ) {
            throw new BadRequestException(
                    "Mã voucher đã tồn tại"
            );
        }

        voucher.setCode(code);
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMinOrderAmount(request.getMinOrderAmount());
        voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        voucher.setStartDate(request.getStartDate());
        voucher.setEndDate(request.getEndDate());
        voucher.setQuantity(request.getQuantity());

        return voucherMapper.toResponse(voucher);
    }

    @Transactional
    public VoucherResponse deactivate(
            Long id
    ) {

        Voucher voucher =
                getPlatformVoucherForUpdate(id);

        if (
                !Boolean.TRUE.equals(
                        voucher.getActive()
                )
        ) {
            throw new BadRequestException(
                    "Voucher đã bị tắt"
            );
        }

        voucher.setActive(false);

        return voucherMapper.toResponse(voucher);
    }

    @Transactional(readOnly = true)
    public VoucherResponse getByCode(
            String code
    ) {

        Voucher voucher =
                voucherRepository
                        .findByCodeIgnoreCaseAndScope(
                                code,
                                VoucherScope.PLATFORM
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy voucher toàn sàn"
                                )
                        );

        return voucherMapper.toResponse(voucher);
    }

    public Voucher getEntityByCode(String code) {
        return voucherRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher"));
    }

    private Voucher getEntityByCodeForUpdate(String code) {
        return voucherRepository.findByCodeForUpdate(code)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher"));
    }

    private void validateRequest(
            VoucherRequest request
    ) {
        if (
                !request
                        .getEndDate()
                        .isAfter(
                                request.getStartDate()
                        )
        ) {
            throw new BadRequestException(
                    "Ngày kết thúc phải sau ngày bắt đầu"
            );
        }

        if (
                request.getDiscountType()
                        == DiscountType.PERCENTAGE
                        &&
                        request.getDiscountValue()
                                .compareTo(
                                        BigDecimal.valueOf(100)
                                ) > 0
        ) {
            throw new BadRequestException(
                    "Phần trăm giảm không được vượt quá 100"
            );
        }

        if (
                request.getMinOrderAmount() != null
                        &&
                        request.getMinOrderAmount()
                                .compareTo(
                                        BigDecimal.ZERO
                                ) < 0
        ) {
            throw new BadRequestException(
                    "Giá trị đơn tối thiểu không hợp lệ"
            );
        }

        if (
                request.getMaxDiscountAmount() != null
                        &&
                        request.getMaxDiscountAmount()
                                .compareTo(
                                        BigDecimal.ZERO
                                ) < 0
        ) {
            throw new BadRequestException(
                    "Mức giảm tối đa không hợp lệ"
            );
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal previewDiscount(
            String code,
            BigDecimal orderAmount
    ) {
        if (
                code == null ||
                        code.isBlank()
        ) {
            return BigDecimal.ZERO;
        }

        if (
                orderAmount == null ||
                        orderAmount.compareTo(
                                BigDecimal.ZERO
                        ) < 0
        ) {
            throw new BadRequestException(
                    "Giá trị đơn hàng không hợp lệ"
            );
        }

        Voucher voucher =
                getEntityByCode(code);

        validateUsable(voucher);

        validateMinimumOrder(
                voucher,
                orderAmount
        );

        return calculateVoucherAmount(
                voucher,
                orderAmount
        );
    }

    private void validateMinimumOrder(
            Voucher voucher,
            BigDecimal amount
    ) {
        if (
                voucher.getMinOrderAmount()
                        != null
                        &&
                        amount.compareTo(
                                voucher.getMinOrderAmount()
                        ) < 0
        ) {
            throw new BadRequestException(
                    "Giá trị sản phẩm chưa đạt điều kiện dùng voucher"
            );
        }
    }

    @Transactional
    public VoucherApplicationResult applyToOrder(
            String code,
            Order order
    ) {
        if (code == null || code.isBlank()) {
            return new VoucherApplicationResult(
                    null,
                    null,
                    null,
                    BigDecimal.ZERO
            );
        }

        Voucher voucher = getEntityByCodeForUpdate(code);
        validateUsable(voucher);
        BigDecimal eligibleAmount;
        Long shopId = null;

        if (voucher.getScope() == VoucherScope.PLATFORM) {
            eligibleAmount = order.getTotalAmount();
        } else if (
                voucher.getScope() == VoucherScope.SHOP
        ) {
            Shop shop = voucher.getShop();

            if (shop == null) {
                throw new BadRequestException("Voucher của shop không hợp lệ");
            }

            ShopOrder shopOrder = order.getShopOrders()
                            .stream()
                            .filter(item ->
                                    item.getShop().getId().equals(shop.getId()))
                            .findFirst()
                            .orElseThrow(() -> new BadRequestException(
                                                    "Đơn hàng không có sản phẩm thuộc shop của voucher"
                                            )
                            );
            eligibleAmount = shopOrder.getSubtotal();
            shopId = shop.getId();

        } else {
            throw new BadRequestException("Phạm vi voucher không hợp lệ");
        }

        validateMinimumOrder(voucher, eligibleAmount);
        BigDecimal discount = calculateVoucherAmount(voucher, eligibleAmount);

        return new VoucherApplicationResult(
                voucher.getCode(),
                voucher.getScope(),
                shopId,
                discount
        );
    }
    private void validateUsable(Voucher voucher) {
        LocalDateTime now = LocalDateTime.now();

        if (!Boolean.TRUE.equals(voucher.getActive())) {
            throw new BadRequestException("Voucher không hoạt động");
        }

        if (now.isBefore(voucher.getStartDate())
                || now.isAfter(voucher.getEndDate())) {
            throw new BadRequestException(
                    "Voucher chưa bắt đầu hoặc đã hết hạn"
            );
        }
    }

    private BigDecimal calculateVoucherAmount(
            Voucher voucher,
            BigDecimal amount
    ) {
        BigDecimal discount;

        if (voucher.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = amount
                    .multiply(voucher.getDiscountValue())
                    .divide(
                            BigDecimal.valueOf(100),
                            2,
                            RoundingMode.HALF_UP
                    );
        } else {
            discount = voucher.getDiscountValue();
        }

        if (voucher.getMaxDiscountAmount() != null
                && discount.compareTo(
                voucher.getMaxDiscountAmount()
        ) > 0) {
            discount = voucher.getMaxDiscountAmount();
        }

        return discount.min(amount);
    }

    @Transactional(readOnly = true)
    public BigDecimal previewDiscountForOrder(
            String code,
            Order order
    ) {
        if (code == null || code.isBlank()) {
            return BigDecimal.ZERO;
        }

        Voucher voucher = getEntityByCode(code);
        validateUsable(voucher);
        BigDecimal eligibleAmount;

        if (voucher.getScope() == VoucherScope.PLATFORM) {
            eligibleAmount = order.getTotalAmount();

        } else if (voucher.getScope() == VoucherScope.SHOP) {
            Shop shop = voucher.getShop();

            if (shop == null) {
                throw new BadRequestException(
                        "Voucher của shop không hợp lệ"
                );
            }

            ShopOrder shopOrder = order.getShopOrders()
                            .stream()
                            .filter(item -> item.getShop().getId().equals(shop.getId()))
                            .findFirst()
                            .orElseThrow(() -> new BadRequestException(
                                                    "Đơn hàng không có sản phẩm thuộc shop của voucher"
                                            )
                            );

            eligibleAmount = shopOrder.getSubtotal();

        } else {
            throw new BadRequestException(
                    "Phạm vi voucher không hợp lệ"
            );
        }
        validateMinimumOrder(voucher, eligibleAmount);

        return calculateVoucherAmount(voucher, eligibleAmount);
    }

    @Transactional(readOnly = true)
    public List<VoucherResponse>
    getAvailableCatalog(
            VoucherScope scope,
            Long shopId
    ) {
        if (
                shopId != null
                        &&
                        shopId <= 0
        ) {
            throw new BadRequestException(
                    "shopId không hợp lệ"
            );
        }
        if (
                shopId != null
                        &&
                        scope == VoucherScope.PLATFORM
        ) {
            throw new BadRequestException(
                    "Voucher toàn sàn không thuộc một shop cụ thể"
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        return voucherRepository
                .findAvailableCatalog(
                        now,
                        scope,
                        shopId
                )
                .stream()
                .map(
                        voucherMapper::toResponse
                )
                .toList();
    }

    @Transactional
    public Voucher claimOne(
            String code
    ) {
        Voucher voucher =
                getEntityByCodeForUpdate(
                        code
                );

        validateCollectable(
                voucher
        );

        voucher.setQuantity(
                voucher.getQuantity() - 1
        );

        return voucher;
    }
    private void validateCollectable(
            Voucher voucher
    ) {
        LocalDateTime now =
                LocalDateTime.now();

        if (
                !Boolean.TRUE.equals(
                        voucher.getActive()
                )
        ) {
            throw new BadRequestException(
                    "Voucher không hoạt động"
            );
        }

        if (
                voucher.getEndDate() != null
                        &&
                        now.isAfter(
                                voucher.getEndDate()
                        )
        ) {
            throw new BadRequestException(
                    "Voucher đã hết hạn"
            );
        }

        if (
                voucher.getQuantity() == null
                        ||
                        voucher.getQuantity() <= 0
        ) {
            throw new BadRequestException(
                    "Voucher đã hết lượt"
            );
        }
    }
    private Voucher getPlatformVoucherForUpdate(
            Long id
    ) {

        return voucherRepository
                .findByIdAndScopeForUpdate(
                        id,
                        VoucherScope.PLATFORM
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy voucher toàn sàn"
                        )
                );
    }

    private boolean hasIssuedConditionChanged(
            Voucher voucher,
            VoucherRequest request,
            String code
    ) {

        if (
                !voucher.getCode().equalsIgnoreCase(code)
        ) {
            return true;
        }

        if (
                voucher.getDiscountType() != request.getDiscountType()
        ) {
            return true;
        }

        if (
                !sameAmount(voucher.getDiscountValue(), request.getDiscountValue())
        ) {
            return true;
        }

        if (
                !sameAmount(voucher.getMinOrderAmount(), request.getMinOrderAmount())
        ) {
            return true;
        }

        if (
                !sameAmount(voucher.getMaxDiscountAmount(), request.getMaxDiscountAmount())
        ) {
            return true;
        }

        if (
                !java.util.Objects.equals(voucher.getStartDate(), request.getStartDate())
        ) {
            return true;
        }

        return !java.util.Objects.equals(voucher.getEndDate(), request.getEndDate());
    }

    private boolean sameAmount(
            BigDecimal first,
            BigDecimal second
    ) {

        if (
                first == null
                        && second == null
        ) {
            return true;
        }

        if (
                first == null
                        || second == null
        ) {
            return false;
        }

        return first.compareTo(second) == 0;
    }
}