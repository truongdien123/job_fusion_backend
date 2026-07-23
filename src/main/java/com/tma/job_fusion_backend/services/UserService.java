package com.tma.job_fusion_backend.services;

import com.tma.job_fusion_backend.pojo.dtos.StaffFilter;
import com.tma.job_fusion_backend.pojo.requests.PagingRequest;
import com.tma.job_fusion_backend.pojo.requests.StaffRequest;
import com.tma.job_fusion_backend.pojo.responses.PageResponse;
import com.tma.job_fusion_backend.pojo.responses.UserResponse;
import com.tma.job_fusion_backend.pojo.responses.TenantStaffLimitResponse;
import java.util.UUID;

public interface UserService {
    UserResponse getUserDetail(UUID id);
    UserResponse createStaff(StaffRequest request);
    PageResponse<UserResponse> getListStaff(PagingRequest<StaffFilter> request);
    UserResponse updateStaff(UUID id, StaffRequest request);
    void deleteStaff(UUID id);
    void resendStaffActivation(UUID id);
    TenantStaffLimitResponse getTenantStaffLimit();
}
