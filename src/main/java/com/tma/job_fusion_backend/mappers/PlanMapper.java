package com.tma.job_fusion_backend.mappers;

import com.tma.job_fusion_backend.models.Plan;
import com.tma.job_fusion_backend.pojo.requests.PlanRequest;
import com.tma.job_fusion_backend.pojo.responses.PlanResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PlanMapper {

    @Mapping(target = "features", ignore = true)
    @Mapping(target = "staffAccountUnlimited", expression = "java(plan.getMaxStaffAccount() == null)")
    @Mapping(target = "activeJobPostingUnlimited", expression = "java(plan.getMaxActiveJobPosting() == null)")
    PlanResponse toPlanResponse(Plan plan);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "feature", ignore = true)
    Plan toEntity(PlanRequest request);

}
