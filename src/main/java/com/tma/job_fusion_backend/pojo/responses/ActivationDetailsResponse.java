package com.tma.job_fusion_backend.pojo.responses;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivationDetailsResponse {
    private String workspaceName;
    private String role;
    private String email;
}
