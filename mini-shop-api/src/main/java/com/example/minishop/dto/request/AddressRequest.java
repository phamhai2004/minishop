package com.example.minishop.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AddressRequest {

    @NotBlank(message = "Tên người nhận không được bỏ trống")
    private String receiverName;
    @NotBlank(message = "Số điện thoại không được bỏ trống")
    @Pattern(
            regexp = "^$|^[0-9]{9,11}$",
            message = "Số điện thoại không hợp lệ"
    )
    private String phone;
    @NotBlank(message = "Tỉnh/Thành phố không được bỏ trống")
    private String province;
    @NotBlank(message = "Phường/Xã không được bỏ trống")
    private String ward;
    @NotBlank(message = "Địa chỉ chi tiết không được bỏ trống")
    private String detail;
    @DecimalMin(
            value = "-90.0",
            message = "Vĩ độ không hợp lệ"
    )
    @DecimalMax(
            value = "90.0",
            message = "Vĩ độ không hợp lệ"
    )
    private Double latitude;
    @DecimalMin(
            value = "-180.0",
            message = "Kinh độ không hợp lệ"
    )
    @DecimalMax(
            value = "180.0",
            message = "Kinh độ không hợp lệ"
    )
    private Double longitude;
    private Boolean defaultAddress;

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Boolean getDefaultAddress() {
        return defaultAddress;
    }

    public void setDefaultAddress(Boolean defaultAddress) {
        this.defaultAddress = defaultAddress;
    }
}