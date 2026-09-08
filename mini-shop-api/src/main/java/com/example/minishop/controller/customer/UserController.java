package com.example.minishop.controller.customer;

import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.request.RegisterRequest;
import com.example.minishop.dto.request.RegistrationEmailRequest;
import com.example.minishop.dto.request.UpdateUserRequest;
import com.example.minishop.dto.response.UserResponse;
import com.example.minishop.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        userService.getCurrentUser()
                )
        );
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            @Valid @RequestBody UpdateUserRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        userService.updateCurrentUser(request)
                )
        );
    }

    @GetMapping("/email/verify")
    public ResponseEntity<ApiResponse<String>> verifyCurrentUserEmail(
            @RequestParam String token
    ) {

        String email = userService.verifyCurrentUserEmail(token);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Xác minh email thành công",
                        email
                )
        );
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        UserResponse response = userService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký thành công", response));
    }

    @PostMapping("/email/resend")
    public ResponseEntity<ApiResponse<Void>>
    resendCurrentUserEmailVerification(
            @RequestParam String token
    ) {

        userService.resendCurrentUserEmailVerification(
                token
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đã gửi lại email xác minh",
                        null
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success(userService.getAll())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(userService.getById(id))
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<UserResponse>> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Khóa tài khoản thành công", userService.deactivate(id))
        );
    }

    @PostMapping("/register/request-email")
    public ResponseEntity<ApiResponse<Void>> requestRegistrationEmail(
            @Valid @RequestBody RegistrationEmailRequest request
    ) {
        userService.requestRegistrationEmail(request.getEmail());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đã gửi email xác minh",
                        null
                )
        );
    }

    @GetMapping("/register/verify")
    public ResponseEntity<ApiResponse<String>> verifyRegistrationEmail(
            @RequestParam String token
    ) {
        String email = userService.verifyRegistrationEmail(token);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Xác minh email thành công",
                        email
                )
        );
    }

    @PostMapping(
            value = "/me/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> updateAvatar(
            @RequestPart("file")
            MultipartFile file
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật ảnh đại diện thành công",
                        userService.updateAvatar(
                                file
                        )
                )
        );
    }

}