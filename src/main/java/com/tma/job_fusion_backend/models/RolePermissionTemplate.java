package com.tma.job_fusion_backend.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_permission_template")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionTemplate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_template_id")
    private RoleTemplate roleTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id")
    private Permission permission;

}
