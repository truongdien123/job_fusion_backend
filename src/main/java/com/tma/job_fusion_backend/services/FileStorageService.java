package com.tma.job_fusion_backend.services;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String uploadFile(MultipartFile file);
    String uploadFile(MultipartFile file, String folder);
}
