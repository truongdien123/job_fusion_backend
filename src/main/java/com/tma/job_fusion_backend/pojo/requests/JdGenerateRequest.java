package com.tma.job_fusion_backend.pojo.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JdGenerateRequest {

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Location type is required")
    private String locationType; // e.g. "Office", "Remote", "Hybrid"

    private String salaryRange; // e.g. "$1000 - $2000" (optional)

    @NotEmpty(message = "Key skills must not be empty")
    private List<String> keySkills;

    private String additionalRequirements; // optional
}
