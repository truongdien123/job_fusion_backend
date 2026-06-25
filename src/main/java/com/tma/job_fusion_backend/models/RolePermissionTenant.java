package com.tma.job_fusion_backend.models;

import com.tma.job_fusion_backend.models.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_permission_tenant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermissionTenant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_tenant_id")
    private RoleTenant roleTenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id")
    private Permission permission;

}
