package com.tma.job_fusion_backend.controllers;

import com.tma.job_fusion_backend.commons.EndpointConstant;
import com.tma.job_fusion_backend.services.FileStorageService;
import com.tma.job_fusion_backend.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(EndpointConstant.ENDPOINT_UPLOAD)
@RequiredArgsConstructor
@Tag(name = "upload-file")
public class UploadController {

    private final FileStorageService fileStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder) {
        String fileUrl = fileStorageService.uploadFile(file, folder);
        return ResponseUtil.success("File uploaded successfully", fileUrl);
    }
}
