package com.tma.job_fusion_backend.pojo.requests;

import com.tma.job_fusion_backend.annotations.FutureOrPresentDate;
import com.tma.job_fusion_backend.enums.EmploymentType;
import com.tma.job_fusion_backend.enums.JobStatus;
import com.tma.job_fusion_backend.enums.LocationType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostingRequest {

    @NotBlank(message = "Job title is required")
    @Size(max = 255, message = "Job title length cannot exceed 255 characters")
    private String title;

    @NotBlank(message = "Department is required")
    @Size(max = 255, message = "Department length cannot exceed 255 characters")
    private String department;

    @Size(max = 255, message = "Level length cannot exceed 255 characters")
    private String level;

    @NotNull(message = "Employment type is required")
    private EmploymentType employmentType;

    @NotNull(message = "Location type is required")
    private LocationType locationType;

    @NotBlank(message = "Location is required")
    @Size(max = 255, message = "Location length cannot exceed 255 characters")
    private String location;

    @FutureOrPresentDate(message = "Application deadline must be in the future or present")
    private LocalDateTime applicationDeadline;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Requirements is required")
    private String requirements;

    private String benefits;

    @DecimalMin(value = "0.0", message = "Salary min must be greater than or equal to 0")
    private Double salaryMin;

    @DecimalMin(value = "0.0", message = "Salary max must be greater than or equal to 0")
    private Double salaryMax;

    private JobStatus status;
}
