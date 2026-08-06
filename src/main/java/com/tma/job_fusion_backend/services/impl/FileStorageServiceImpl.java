package com.tma.job_fusion_backend.services.impl;

import com.cloudinary.Cloudinary;
import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.exceptions.BadRequestException;
import com.tma.job_fusion_backend.exceptions.FileUploadException;
import com.tma.job_fusion_backend.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class FileStorageServiceImpl implements FileStorageService {

    private final Cloudinary cloudinary;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp",
            ".pdf",
            ".docx", ".doc"
    );

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp",
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword"
    );

    @Override
    public String uploadFile(MultipartFile file) {
        return uploadFile(file, null);
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        validateFile(file);

        Map<String, Object> options = com.cloudinary.utils.ObjectUtils.asMap(
                "resource_type", "auto"
        );

        if (StringUtils.isNotEmpty(folder)) {
            options.put("folder", folder);
        }

        try {
            log.info("Uploading file '{}' to Cloudinary (folder: {})", file.getOriginalFilename(), folder);
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            
            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("Successfully uploaded file: {}, public_id: {}, resource_type: {}, url: {}",
                    file.getOriginalFilename(),
                    uploadResult.get("public_id"),
                    uploadResult.get("resource_type"),
                    secureUrl);

            return secureUrl;
        } catch (IOException e) {
            log.error("Cloudinary upload failed for file '{}': {}", file.getOriginalFilename(), e.getMessage());
            throw new FileUploadException(ErrorCode.FILE_UPLOAD_FAILED, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (ObjectUtils.isEmpty(file)) {
            throw new BadRequestException(ErrorCode.FILE_EMPTY);
        }

        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isEmpty(originalFilename)) {
            throw new BadRequestException(ErrorCode.FILE_INVALID_NAME);
        }

        // Validate file extension
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException(ErrorCode.FILE_EXTENSION_NOT_ALLOWED);
        }

        // Validate content type
        String contentType = file.getContentType();
        if (StringUtils.isEmpty(contentType) || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException(ErrorCode.FILE_CONTENT_TYPE_NOT_ALLOWED);
        }
    }
}
