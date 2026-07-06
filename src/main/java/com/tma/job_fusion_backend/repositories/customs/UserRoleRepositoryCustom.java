package com.tma.job_fusion_backend.repositories.customs;

import com.tma.job_fusion_backend.models.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepositoryCustom {
    Optional<User> findTenantAdminByTenantId(UUID tenantId);
}
