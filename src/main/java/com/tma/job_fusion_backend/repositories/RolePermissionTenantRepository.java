package com.tma.job_fusion_backend.repositories;

import com.tma.job_fusion_backend.models.RolePermissionTenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RolePermissionTenantRepository extends JpaRepository<RolePermissionTenant, UUID> {
}
