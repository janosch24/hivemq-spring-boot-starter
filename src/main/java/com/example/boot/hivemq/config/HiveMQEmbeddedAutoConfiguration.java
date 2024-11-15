package com.example.boot.hivemq.config;

import lombok.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Value
@AutoConfiguration
@ConditionalOnClass(com.hivemq.HiveMQServer.class)
@EnableConfigurationProperties(HiveMQEmbeddedProperties.class)
public class HiveMQEmbeddedAutoConfiguration {

    HiveMQEmbeddedProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public HiveMQEmbeddedService hiveMQEmbeddedService() {
        return new HiveMQEmbeddedService(this.properties);
    }
}