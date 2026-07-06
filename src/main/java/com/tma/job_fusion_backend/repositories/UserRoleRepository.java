package com.tma.job_fusion_backend.repositories;

import com.tma.job_fusion_backend.models.User;
import com.tma.job_fusion_backend.models.UserRole;
import com.tma.job_fusion_backend.repositories.customs.UserRoleRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID>, UserRoleRepositoryCustom {
    Optional<UserRole> findByUser(User user);
}
