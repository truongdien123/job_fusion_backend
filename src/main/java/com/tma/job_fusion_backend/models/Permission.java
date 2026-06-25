package com.tma.job_fusion_backend.models;

import com.tma.job_fusion_backend.enums.PermissionScope;
import com.tma.job_fusion_backend.models.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends BaseEntity {

    private String name;

    private String description;

    private String module;

    @Enumerated(EnumType.STRING)
    private PermissionScope scope;

}
