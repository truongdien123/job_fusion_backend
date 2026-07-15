package com.tma.job_fusion_backend.repositories;

import com.tma.job_fusion_backend.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByIdAndDeletedAtIsNull(UUID id);
    List<User> findAllByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Page<User> findAllByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    long countByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
