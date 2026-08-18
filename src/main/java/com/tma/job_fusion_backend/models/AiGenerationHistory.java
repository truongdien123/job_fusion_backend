package com.tma.job_fusion_backend.models;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;

@Entity
@Table(name = "ai_generation_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiGenerationHistory extends BaseEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "feature_type", nullable = false)
    private String featureType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "prompt_input", columnDefinition = "jsonb", nullable = false)
    private JsonNode promptInput;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "generated_output", columnDefinition = "jsonb", nullable = false)
    private JsonNode generatedOutput;
}
