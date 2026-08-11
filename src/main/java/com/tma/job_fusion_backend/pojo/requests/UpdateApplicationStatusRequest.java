package com.tma.job_fusion_backend.pojo.requests;

import com.tma.job_fusion_backend.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateApplicationStatusRequest {

    @NotNull(message = "Status is required")
    private ApplicationStatus status;
}
