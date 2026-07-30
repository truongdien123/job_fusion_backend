package com.tma.job_fusion_backend.components;

import com.tma.job_fusion_backend.repositories.query.JobPostingQueryRepository;
import com.tma.job_fusion_backend.utils.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Log4j2
public class JobPostingScheduler {

    private final JobPostingQueryRepository jobPostingQueryRepository;

    @Scheduled(cron = "${app.cron.close-expired-jobs:0 0 * * * *}")
    public void closeExpiredJobs() {
        log.info("Starting cron job to close expired job postings...");
        LocalDateTime now = DateTimeUtil.nowUtc();
        int updatedCount = jobPostingQueryRepository.closeExpiredJobPostings(now);
        log.info("Cron job completed. Closed {} expired job postings.", updatedCount);
    }
}
