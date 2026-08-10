package com.tma.job_fusion_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.type.format.jackson.JacksonJsonFormatMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import java.util.Map;

@Configuration
public class HibernateJacksonConfig implements BeanPostProcessor {

    private final ObjectProvider<Jackson3SupportConfig> supportConfigProvider;
    private ObjectMapper objectMapper;

    public HibernateJacksonConfig(ObjectProvider<Jackson3SupportConfig> supportConfigProvider) {
        this.supportConfigProvider = supportConfigProvider;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof LocalContainerEntityManagerFactoryBean factoryBean) {
            if (ObjectUtils.isEmpty(objectMapper)) {
                objectMapper = new ObjectMapper();
                Jackson3SupportConfig supportConfig = supportConfigProvider.getIfAvailable();
                if (supportConfig != null) {
                    objectMapper.registerModule(supportConfig.jackson3Module());
                }
            }
            Map<String, Object> jpaPropertyMap = factoryBean.getJpaPropertyMap();
            jpaPropertyMap.put("hibernate.type.json_format_mapper", new JacksonJsonFormatMapper(objectMapper));
        }
        return bean;
    }
}
