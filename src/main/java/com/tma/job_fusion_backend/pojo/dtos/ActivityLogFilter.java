package com.tma.job_fusion_backend.pojo.dtos;

import com.tma.job_fusion_backend.enums.EventType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLogFilter {
    private EventType eventType;
    private String search;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private UUID tenantId;
}
