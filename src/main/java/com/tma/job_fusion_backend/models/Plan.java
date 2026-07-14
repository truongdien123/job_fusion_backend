package com.tma.job_fusion_backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.tma.job_fusion_backend.enums.PlanStatus;

@Entity
@Table(name = "plan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Plan extends BaseEntity {

    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "monthly_price")
    private Double monthlyPrice;

    @Column(name = "max_staff_account")
    private Integer maxStaffAccount;

    @Column(name = "max_active_job_posting")
    private Integer maxActiveJobPosting;

    @Enumerated(EnumType.STRING)
    private PlanStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode feature;

}
