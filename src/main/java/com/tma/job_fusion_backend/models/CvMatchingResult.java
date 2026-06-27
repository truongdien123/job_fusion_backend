package com.tma.job_fusion_backend.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "cv_matching_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CvMatchingResult extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private CandidateApplication application;

    @Column(name = "matching_score")
    private Double matchingScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode reasoning;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "skill_gaps")
    private JsonNode skillGaps;

}
