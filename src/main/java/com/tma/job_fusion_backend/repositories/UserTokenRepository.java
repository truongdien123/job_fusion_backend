package com.tma.job_fusion_backend.repositories;

import com.tma.job_fusion_backend.models.UserToken;
import com.tma.job_fusion_backend.repositories.customs.UserTokenRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, UUID>, UserTokenRepositoryCustom {
}
