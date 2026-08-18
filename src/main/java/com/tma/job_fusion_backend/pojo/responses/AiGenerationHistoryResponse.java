package com.tma.job_fusion_backend.pojo.responses;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiGenerationHistoryResponse {
    private UUID id;
    private UUID tenantId;
    private UUID userId;
    private String userFullName;
    private String featureType;
    private JsonNode promptInput;
    private JsonNode generatedOutput;
    private LocalDateTime createdAt;
}
