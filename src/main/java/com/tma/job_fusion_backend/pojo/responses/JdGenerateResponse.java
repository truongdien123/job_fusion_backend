package com.tma.job_fusion_backend.pojo.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JdGenerateResponse {
    private String jobTitle;
    private String overview;
    private List<String> responsibilities;
    private List<String> requirements;
    private List<String> benefits;
}
