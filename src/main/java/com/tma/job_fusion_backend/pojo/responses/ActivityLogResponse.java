package com.tma.job_fusion_backend.pojo.responses;

import com.tma.job_fusion_backend.enums.EventType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLogResponse {
    private UUID id;
    private EventType eventType;
    private String description;
    private String ipAddress;
    private LocalDateTime createdAt;
    private UUID userId;
    private String userFullName;
}
