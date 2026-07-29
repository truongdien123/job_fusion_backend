package com.tma.job_fusion_backend.pojo.responses;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostingRevisionResponse {
    private String action;
    private String actorName;
    private LocalDateTime createdAt;
}
