package com.tma.job_fusion_backend;

import com.tma.job_fusion_backend.annotations.EnableCloudinaryConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCloudinaryConfig
public class JobFusionBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobFusionBackendApplication.class, args);
    }

}
