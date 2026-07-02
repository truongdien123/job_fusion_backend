package com.tma.job_fusion_backend.repositories;

import com.tma.job_fusion_backend.models.User;
import com.tma.job_fusion_backend.models.RoleTenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleTenantRepository extends JpaRepository<RoleTenant, UUID> {
    Optional<RoleTenant> findByUser(User user);
}
