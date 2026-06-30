package com.tma.job_fusion_backend.components;

import com.tma.job_fusion_backend.enums.UserStatus;
import com.tma.job_fusion_backend.enums.UserType;
import com.tma.job_fusion_backend.models.RolePlatform;
import com.tma.job_fusion_backend.models.User;
import com.tma.job_fusion_backend.repositories.RolePlatformRepository;
import com.tma.job_fusion_backend.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Configuration
public class DataSeeder implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    @Value("${app.admin.email}")
    private String email;

    @Value("${app.admin.password}")
    private String password;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolePlatformRepository rolePlatformRepository;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder, RolePlatformRepository rolePlatformRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.rolePlatformRepository = rolePlatformRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        seedSuperAdmin();
    }


    private void seedSuperAdmin() {
        if (userRepository.existsByEmail(email)) {
            logger.info("Super Admin already exists, skipping seeding");
            return;
        }
        User admin = new User();
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setType(UserType.PLATFORM);
        admin.setActivatedDate(LocalDateTime.now(ZoneOffset.UTC));
        admin.setStatus(UserStatus.ACTIVE);
        userRepository.save(admin);

        RolePlatform rolePlatform = new RolePlatform("Super Admin", true, "Admin of platform");
        rolePlatformRepository.save(rolePlatform);
        logger.info("Super admin have been created successfully!");
    }
}
