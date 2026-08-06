package com.tma.job_fusion_backend.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.tma.job_fusion_backend.models.SystemConfig;
import com.tma.job_fusion_backend.repositories.SystemConfigRepository;
import com.tma.job_fusion_backend.utils.EncryptionUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;

import java.util.List;

@Log4j2
public class CloudinaryConfig {

    private final String CLOUD_NAME = "cloudinary.cloud-name";
    private final String API_KEY = "cloudinary.api-key";
    private final String API_SECRET = "cloudinary.api-secret";

    @Bean
    public Cloudinary cloudinary(SystemConfigRepository systemConfigRepository, EncryptionUtil encryptionUtil) {
        log.info("Initializing Cloudinary bean using database configurations with local properties fallback");

        List<SystemConfig> configs = systemConfigRepository.findByConfigGroupAndDeletedAtIsNull("CLOUDINARY");

        String cloudName = configs.stream()
                .filter(c -> CLOUD_NAME.equals(c.getConfigKey()))
                .map(SystemConfig::getConfigValue)
                .filter(StringUtils::isNotEmpty)
                .findFirst()
                .orElse(null);

        String encryptedApiKey = configs.stream()
                .filter(c -> API_KEY.equals(c.getConfigKey()))
                .map(SystemConfig::getConfigValue)
                .filter(StringUtils::isNotEmpty)
                .findFirst()
                .orElse(null);
        String apiKey = encryptionUtil.decrypt(encryptedApiKey);

        String encryptedApiSecret = configs.stream()
                .filter(c -> API_SECRET.equals(c.getConfigKey()))
                .map(SystemConfig::getConfigValue)
                .filter(StringUtils::isNotEmpty)
                .findFirst()
                .orElse(null);
        String apiSecret = encryptionUtil.decrypt(encryptedApiSecret);

        if (StringUtils.isEmpty(cloudName) || StringUtils.isEmpty(apiKey) || StringUtils.isEmpty(apiSecret)) {
            log.warn("Cloudinary configuration properties are incomplete. Uploads may fail.");
        } else {
            log.info("Cloudinary configured successfully. Cloud Name: {}", cloudName);
        }

        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }
}
