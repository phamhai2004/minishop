package com.example.minishop.controller.customer;

import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.request.AddressRequest;
import com.example.minishop.dto.response.AddressResponse;
import com.example.minishop.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getMyAddresses() {
        return ResponseEntity.ok(
                ApiResponse.success(addressService.getMyAddresses())
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> create(
            @Valid @RequestBody AddressRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("Tạo địa chỉ thành công", addressService.create(request))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật địa chỉ thành công", addressService.update(id, request))
        );
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefault(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("Đã đặt làm địa chỉ mặc định", addressService.setDefault(id))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        addressService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success("Xóa địa chỉ thành công", null)
        );
    }

    @GetMapping("/default")
    public ResponseEntity<ApiResponse<AddressResponse>> getDefault() {
        return ResponseEntity.ok(
                ApiResponse.success(addressService.getDefaultAddress())
        );
    }
}