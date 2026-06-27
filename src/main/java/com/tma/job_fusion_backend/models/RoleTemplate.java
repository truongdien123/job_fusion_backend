package com.tma.job_fusion_backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "role_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleTemplate extends BaseEntity {

    private String name;

    private String description;

}
