package com.tma.job_fusion_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    private final Jackson3SupportConfig jackson3SupportConfig;

    public JacksonConfig(Jackson3SupportConfig jackson3SupportConfig) {
        this.jackson3SupportConfig = jackson3SupportConfig;
    }

    @Bean
    @Primary
    public com.fasterxml.jackson.databind.ObjectMapper jackson2ObjectMapper() {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(jackson3SupportConfig.jackson3Module());
        return mapper;
    }

    @Bean
    public tools.jackson.databind.ObjectMapper jackson3ObjectMapper() {
        return new tools.jackson.databind.ObjectMapper();
    }
}
