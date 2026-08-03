package com.tma.job_fusion_backend.repositories;

import com.tma.job_fusion_backend.models.Tenant;
import com.tma.job_fusion_backend.enums.TenantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByIdAndDeletedAtIsNull(UUID id);
    Page<Tenant> findAllByDeletedAtIsNull(Pageable pageable);

    boolean existsByCompanyNameIgnoreCaseAndDeletedAtIsNull(String companyName);

    boolean existsByCompanyNameIgnoreCaseAndIdNotAndDeletedAtIsNull(String companyName, UUID id);

    boolean existsByDomainIgnoreCaseAndDeletedAtIsNull(String domain);

    boolean existsByDomainIgnoreCaseAndIdNotAndDeletedAtIsNull(String domain, UUID id);

    boolean existsByPlanIdAndDeletedAtIsNull(UUID planId);

    List<Tenant> findAllByStatusAndDeletedAtIsNullAndExpirationDateLessThanEqualAndExpirationWarningSentFalse(
            TenantStatus status, LocalDateTime warningThreshold);

    List<Tenant> findAllByStatusAndDeletedAtIsNullAndExpirationDateBefore(
            TenantStatus status, LocalDateTime now);
}
