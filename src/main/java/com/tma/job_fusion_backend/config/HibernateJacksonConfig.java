package com.tma.job_fusion_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.type.format.jackson.JacksonJsonFormatMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import java.util.Map;

@Configuration
public class HibernateJacksonConfig implements BeanPostProcessor {

    private final ObjectMapper objectMapper;

    public HibernateJacksonConfig(Jackson3SupportConfig supportConfig) {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(supportConfig.jackson3Module());
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof LocalContainerEntityManagerFactoryBean factoryBean) {
            Map<String, Object> jpaPropertyMap = factoryBean.getJpaPropertyMap();
            jpaPropertyMap.put("hibernate.type.json_format_mapper", new JacksonJsonFormatMapper(objectMapper));
        }
        return bean;
    }
}
