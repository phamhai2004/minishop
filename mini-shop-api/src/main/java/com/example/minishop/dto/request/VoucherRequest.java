package com.example.minishop.dto.request;

import com.example.minishop.constant.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VoucherRequest {

    @NotBlank(message = "Mã voucher không được bỏ trống")
    private String code;
    @NotNull(message = "Loại giảm giá không được bỏ trống")
    private DiscountType discountType;
    @NotNull(message = "Giá trị giảm không được bỏ trống")
    @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal discountValue;
    @DecimalMin(
            value = "0",
            message =
                    "Giá trị đơn tối thiểu không được âm"
    )
    private BigDecimal minOrderAmount;
    @DecimalMin(
            value = "0",
            message =
                    "Mức giảm tối đa không được âm"
    )
    private BigDecimal maxDiscountAmount;
    @NotNull(message = "Ngày bắt đầu không được bỏ trống")
    private LocalDateTime startDate;
    @NotNull(message = "Ngày kết thúc không được bỏ trống")
    private LocalDateTime endDate;
    @NotNull(message = "Số lượng không được bỏ trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;
    private Boolean active;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public BigDecimal getMinOrderAmount() {
        return minOrderAmount;
    }

    public void setMinOrderAmount(BigDecimal minOrderAmount) {
        this.minOrderAmount = minOrderAmount;
    }

    public BigDecimal getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}