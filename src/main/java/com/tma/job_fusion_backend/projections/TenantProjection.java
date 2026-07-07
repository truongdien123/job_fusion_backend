package com.tma.job_fusion_backend.projections;

import com.tma.job_fusion_backend.enums.TenantStatus;
import java.util.UUID;

public interface TenantProjection {
    UUID getId();
    String getCompanyName();
    String getDomain();
    String getIndustry();
    String getCompanySize();
    String getRegion();
    TenantStatus getStatus();
    UUID getPlanId();
    UUID getAdminUserId();
}
