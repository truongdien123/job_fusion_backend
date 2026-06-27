package com.tma.job_fusion_backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "role_platform")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RolePlatform extends BaseEntity {

    private String name;

    @Column(name = "is_admin")
    private Boolean isAdmin;

    private String description;

}
