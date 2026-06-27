package com.tma.job_fusion_backend.repositories;

import com.tma.job_fusion_backend.models.RolePermissionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RolePermissionTemplateRepository extends JpaRepository<RolePermissionTemplate, UUID> {
}
