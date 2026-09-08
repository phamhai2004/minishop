package com.example.minishop.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.minishop.dto.response.UploadImageResponse;
import com.example.minishop.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageStorageService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final Cloudinary cloudinary;

    public ImageStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public UploadImageResponse uploadImage(
            MultipartFile file,
            String folder
    ) {
        validateImage(file);

        String publicId = UUID.randomUUID().toString();

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "public_id", publicId,
                            "resource_type", "image",
                            "overwrite", false
                    )
            );

            return new UploadImageResponse(
                    String.valueOf(result.get("secure_url")),
                    String.valueOf(result.get("public_id")),
                    String.valueOf(result.get("format")),
                    toLong(result.get("bytes"))
            );
        } catch (IOException exception) {
            throw new BadRequestException(
                    "Không thể tải ảnh lên hệ thống"
            );
        }
    }

    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "invalidate", true
                    )
            );
        } catch (IOException exception) {
            throw new BadRequestException(
                    "Không thể xóa ảnh khỏi hệ thống"
            );
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(
                    "File ảnh không được bỏ trống"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException(
                    "Ảnh không được vượt quá 5MB"
            );
        }

        String contentType = file.getContentType();

        if (contentType == null
                || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException(
                    "Chỉ chấp nhận ảnh JPG, PNG hoặc WEBP"
            );
        }
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }

        return null;
    }
}