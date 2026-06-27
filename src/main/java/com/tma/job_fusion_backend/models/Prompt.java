package com.tma.job_fusion_backend.models;

import com.tma.job_fusion_backend.enums.PromptType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prompts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Prompt extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "prompt_type")
    private PromptType promptType;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String prompt;

    private Integer version;

    private Boolean active;

}
