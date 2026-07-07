package com.tma.job_fusion_backend.repositories;

import com.tma.job_fusion_backend.models.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Page<Tenant> findAllByDeletedAtIsNull(Pageable pageable);
}
