package com.tma.job_fusion_backend.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleTenant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_template_id")
    private RoleTemplate roleTemplate;

    private String name;

    private String description;

}
