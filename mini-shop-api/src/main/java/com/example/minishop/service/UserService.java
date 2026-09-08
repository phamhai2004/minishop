package com.example.minishop.service;

import com.example.minishop.constant.Role;
import com.example.minishop.dto.request.RegisterRequest;
import com.example.minishop.dto.request.UpdateUserRequest;
import com.example.minishop.dto.response.UploadImageResponse;
import com.example.minishop.dto.response.UserResponse;
import com.example.minishop.entity.EmailVerificationToken;
import com.example.minishop.entity.RegistrationVerification;
import com.example.minishop.entity.User;
import com.example.minishop.exception.BadRequestException;
import com.example.minishop.exception.ResourceNotFoundException;
import com.example.minishop.mapper.UserMapper;
import com.example.minishop.repository.EmailVerificationTokenRepository;
import com.example.minishop.repository.RegistrationVerificationRepository;
import com.example.minishop.repository.UserRepository;
import com.example.minishop.security.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationVerificationRepository registrationVerificationRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService;
    private final ImageStorageService imageStorageService;

    public UserService(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            RegistrationVerificationRepository registrationVerificationRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            EmailService emailService,
            ImageStorageService imageStorageService
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.registrationVerificationRepository = registrationVerificationRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.emailService = emailService;
        this.imageStorageService = imageStorageService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã tồn tại");
        }

        RegistrationVerification verification =
                registrationVerificationRepository
                        .findByEmail(request.getEmail())
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Email chưa được xác minh"
                                )
                        );

        if (!Boolean.TRUE.equals(verification.getVerified())) {
            throw new BadRequestException(
                    "Email chưa được xác minh"
            );
        }

        User user = userMapper.toEntity(request);

        user.setRole(Role.CUSTOMER);
        user.setCreatedAt(LocalDateTime.now());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setEmailVerified(true);

        User savedUser = userRepository.save(user);

        registrationVerificationRepository.deleteByEmail(
                request.getEmail()
        );

        return userMapper.toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = getEntityById(id);
        return userMapper.toResponse(user);
    }

    public User getEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy người dùng với id: " + id
                ));
    }

    @Transactional
    public UserResponse deactivate(Long id) {
        User user = getEntityById(id);

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new BadRequestException("Tài khoản đã bị khóa");
        }

        user.setActive(false);

        return userMapper.toResponse(user);
    }

    @Transactional
    public void requestRegistrationEmail(String email) {

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email đã tồn tại");
        }

        registrationVerificationRepository.deleteByEmail(email);

        String token = UUID.randomUUID().toString();

        RegistrationVerification verification =
                new RegistrationVerification();

        verification.setEmail(email);
        verification.setToken(token);
        verification.setVerified(false);
        verification.setExpiresAt(
                LocalDateTime.now().plusMinutes(15)
        );
        verification.setCreatedAt(LocalDateTime.now());

        registrationVerificationRepository.save(verification);

        try {
            emailService.sendRegistrationVerificationEmail(
                    verification
            );
        } catch (Exception e) {
            throw new BadRequestException(
                    "Không thể gửi email xác minh"
            );
        }
    }

    @Transactional
    public String verifyRegistrationEmail(String token) {

        RegistrationVerification verification =
                registrationVerificationRepository
                        .findByToken(token)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Token xác minh không hợp lệ"
                                )
                        );

        if (Boolean.TRUE.equals(verification.getVerified())) {
            throw new BadRequestException(
                    "Email đã được xác minh"
            );
        }

        if (verification.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new BadRequestException(
                    "Link xác minh email đã hết hạn"
            );
        }

        verification.setVerified(true);

        registrationVerificationRepository.save(verification);

        return verification.getEmail();
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {

        User user = getCurrentUserEntity();

        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateCurrentUser(
            UpdateUserRequest request
    ) {

        User user = getCurrentUserEntity();

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());

        String newEmail = request.getEmail();

        if (newEmail != null) {
            newEmail = newEmail.trim();
        }

        String currentEmail = user.getEmail();

        if (newEmail != null
                && !newEmail.equalsIgnoreCase(currentEmail)) {

            if (userRepository.existsByEmail(newEmail)) {
                throw new BadRequestException(
                        "Email đã được sử dụng"
                );
            }

            user.setEmail(newEmail);
            user.setEmailVerified(false);

            EmailVerificationToken verificationToken =
                    emailVerificationTokenRepository
                            .findByUser_Id(user.getId())
                            .orElseGet(EmailVerificationToken::new);

            String token = UUID.randomUUID().toString();

            verificationToken.setUser(user);
            verificationToken.setEmail(newEmail);
            verificationToken.setToken(token);
            verificationToken.setExpiresAt(
                    LocalDateTime.now().plusMinutes(15)
            );
            verificationToken.setUsed(false);
            verificationToken.setCreatedAt(
                    LocalDateTime.now()
            );

            emailVerificationTokenRepository.save(verificationToken);

            try {
                emailService.sendEmailChangeVerificationEmail(
                        verificationToken
                );
            } catch (Exception e) {
                throw new BadRequestException(
                        "Không thể gửi email xác minh"
                );
            }
        }

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public String verifyCurrentUserEmail(String token) {

        EmailVerificationToken verificationToken =
                emailVerificationTokenRepository
                        .findByToken(token)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Token xác minh không hợp lệ"
                                )
                        );

        if (Boolean.TRUE.equals(verificationToken.getUsed())) {
            throw new BadRequestException(
                    "Token xác minh đã được sử dụng"
            );
        }

        if (verificationToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new BadRequestException(
                    "Link xác minh email đã hết hạn"
            );
        }

        String email = verificationToken.getEmail();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy người dùng với email này"
                        )
                );

        user.setEmailVerified(true);

        userRepository.save(user);

        verificationToken.setUsed(true);

        emailVerificationTokenRepository.save(
                verificationToken
        );

        return email;
    }

    @Transactional
    public void resendCurrentUserEmailVerification(
            String token
    ) {

        EmailVerificationToken verificationToken =
                emailVerificationTokenRepository
                        .findByToken(token)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Token xác minh không hợp lệ"
                                )
                        );

        User user = verificationToken.getUser();

        if (user == null) {
            throw new BadRequestException(
                    "Token xác minh không hợp lệ"
            );
        }

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BadRequestException(
                    "Email đã được xác minh"
            );
        }

        String email = verificationToken.getEmail();

        if (email == null
                || user.getEmail() == null
                || !email.equalsIgnoreCase(user.getEmail())) {

            throw new BadRequestException(
                    "Email xác minh không còn hợp lệ"
            );
        }

        String newToken =
                UUID.randomUUID().toString();

        verificationToken.setToken(newToken);
        verificationToken.setExpiresAt(
                LocalDateTime.now().plusMinutes(15)
        );
        verificationToken.setUsed(false);
        verificationToken.setCreatedAt(
                LocalDateTime.now()
        );

        emailVerificationTokenRepository.save(
                verificationToken
        );

        try {

            emailService.sendEmailChangeVerificationEmail(
                    verificationToken
            );

        } catch (Exception e) {

            throw new BadRequestException(
                    "Không thể gửi lại email xác minh"
            );
        }
    }

    private User getCurrentUserEntity() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new BadRequestException(
                    "Người dùng chưa đăng nhập"
            );
        }

        String userIdString =
                authentication.getName();

        Long userId;

        try {
            userId = Long.valueOf(userIdString);
        } catch (NumberFormatException e) {

            throw new BadRequestException(
                    "Thông tin userId trong JWT không hợp lệ"
            );
        }

        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy người dùng"
                        )
                );
    }

    @Transactional
    public UserResponse updateAvatar(
            MultipartFile file
    ) {

        User user =
                SecurityUtils
                        .getCurrentUser()
                        .getUser();

        UploadImageResponse uploaded =
                imageStorageService
                        .uploadImage(
                                file,
                                "mini-shop/users/"
                                        + user.getId()
                                        + "/avatar"
                        );

        String oldPublicId =
                user.getAvatarPublicId();

        user.setAvatarUrl(
                uploaded.getUrl()
        );

        user.setAvatarPublicId(
                uploaded.getPublicId()
        );

        User savedUser =
                userRepository.save(
                        user
                );

        if (oldPublicId != null
                && !oldPublicId.isBlank()) {

            imageStorageService.deleteImage(
                    oldPublicId
            );
        }

        return userMapper.toResponse(
                savedUser
        );
    }

}