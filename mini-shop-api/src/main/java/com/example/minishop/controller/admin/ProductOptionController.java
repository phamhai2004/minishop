package com.example.minishop.controller.admin;

import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.request.ProductOptionTypeRequest;
import com.example.minishop.dto.request.ProductOptionValueRequest;
import com.example.minishop.dto.response.ProductOptionTypeResponse;
import com.example.minishop.dto.response.ProductOptionValueResponse;
import com.example.minishop.service.ProductOptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product-options")
public class ProductOptionController {

    private final ProductOptionService productOptionService;

    public ProductOptionController(
            ProductOptionService productOptionService
    ) {
        this.productOptionService = productOptionService;
    }

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<ProductOptionTypeResponse>>>
    getAllTypes() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        productOptionService.getAllTypes()
                )
        );
    }

    @GetMapping("/types/{id}")
    public ResponseEntity<ApiResponse<ProductOptionTypeResponse>>
    getTypeById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        productOptionService.getTypeById(id)
                )
        );
    }

    @PostMapping("/types")
    public ResponseEntity<ApiResponse<ProductOptionTypeResponse>>
    createType(
            @Valid @RequestBody ProductOptionTypeRequest request
    ) {

        ProductOptionTypeResponse response =
                productOptionService.createType(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Tạo phân loại thành công",
                                response
                        )
                );
    }

    @PutMapping("/types/{id}")
    public ResponseEntity<ApiResponse<ProductOptionTypeResponse>>
    updateType(
            @PathVariable Long id,
            @Valid @RequestBody ProductOptionTypeRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật phân loại thành công",
                        productOptionService.updateType(
                                id,
                                request
                        )
                )
        );
    }

    @DeleteMapping("/types/{id}")
    public ResponseEntity<ApiResponse<Void>>
    deleteType(
            @PathVariable Long id
    ) {

        productOptionService.deleteType(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Xóa phân loại thành công",
                        null
                )
        );
    }

    @GetMapping("/types/{typeId}/values")
    public ResponseEntity<ApiResponse<List<ProductOptionValueResponse>>>
    getValuesByType(
            @PathVariable Long typeId
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        productOptionService
                                .getValuesByType(typeId)
                )
        );
    }

    @GetMapping("/values/{id}")
    public ResponseEntity<ApiResponse<ProductOptionValueResponse>>
    getValueById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        productOptionService.getValueById(id)
                )
        );
    }

    @PostMapping("/values")
    public ResponseEntity<ApiResponse<ProductOptionValueResponse>>
    createValue(
            @Valid @RequestBody ProductOptionValueRequest request
    ) {

        ProductOptionValueResponse response =
                productOptionService.createValue(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Tạo giá trị phân loại thành công",
                                response
                        )
                );
    }

    @PutMapping("/values/{id}")
    public ResponseEntity<ApiResponse<ProductOptionValueResponse>>
    updateValue(
            @PathVariable Long id,
            @Valid @RequestBody ProductOptionValueRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật giá trị phân loại thành công",
                        productOptionService.updateValue(
                                id,
                                request
                        )
                )
        );
    }

    @DeleteMapping("/values/{id}")
    public ResponseEntity<ApiResponse<Void>>
    deleteValue(
            @PathVariable Long id
    ) {

        productOptionService.deleteValue(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Xóa giá trị phân loại thành công",
                        null
                )
        );
    }
}