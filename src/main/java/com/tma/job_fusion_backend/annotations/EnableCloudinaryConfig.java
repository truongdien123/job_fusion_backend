package com.tma.job_fusion_backend.annotations;

import com.tma.job_fusion_backend.config.CloudinaryConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CloudinaryConfig.class)
public @interface EnableCloudinaryConfig {
}
