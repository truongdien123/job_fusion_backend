package com.tma.job_fusion_backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "plan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Plan extends BaseEntity {

    private String name;

    @Column(name = "monthly_price")
    private Double monthlyPrice;

    @Column(name = "max_staff_account")
    private Integer maxStaffAccount;

    @Column(name = "max_active_job_posting")
    private Integer maxActiveJobPosting;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode feature;

}
