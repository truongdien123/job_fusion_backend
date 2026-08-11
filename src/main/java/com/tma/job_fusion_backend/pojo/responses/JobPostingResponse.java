package com.tma.job_fusion_backend.pojo.responses;

import com.tma.job_fusion_backend.enums.EmploymentType;
import com.tma.job_fusion_backend.enums.JobStatus;
import com.tma.job_fusion_backend.enums.LocationType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostingResponse {

    private UUID id;

    private UUID tenantId;

    private String tenantName;

    private String title;

    private String department;

    private String level;

    private EmploymentType employmentType;

    private LocationType locationType;

    private String location;

    private LocalDateTime applicationDeadline;

    private String description;

    private String requirements;

    private String benefits;

    private Double salaryMin;

    private Double salaryMax;

    private JobStatus status;

    private Boolean flag;

    private Long numberOfApplicant;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<JobPostingRevisionResponse> revisions;
}
