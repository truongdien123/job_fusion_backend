package com.tma.job_fusion_backend.pojo.dtos;

import com.tma.job_fusion_backend.enums.EmploymentType;
import com.tma.job_fusion_backend.enums.JobStatus;
import com.tma.job_fusion_backend.enums.LocationType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostingFilter {
    private String search;
    private String title;
    private String department;
    private String level;
    private EmploymentType employmentType;
    private LocationType locationType;
    private JobStatus status;
    private UUID tenantId;
}

