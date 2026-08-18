package com.tma.job_fusion_backend.mappers;

import com.tma.job_fusion_backend.models.Plan;
import com.tma.job_fusion_backend.pojo.requests.PlanRequest;
import com.tma.job_fusion_backend.pojo.responses.PlanResponse;
import org.mapstruct.*;

@Mapper(config = GlobalConfigMapper.class)
public interface PlanMapper extends EntityMapper<PlanRequest, PlanResponse, Plan> {

    @Override
    @Mapping(target = "staffAccountUnlimited", expression = "java(plan.getMaxStaffAccount() == null)")
    @Mapping(target = "activeJobPostingUnlimited", expression = "java(plan.getMaxActiveJobPosting() == null)")
    PlanResponse toResponse(Plan plan);

}
