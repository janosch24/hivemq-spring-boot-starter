package com.example.boot.hivemq;

import lombok.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Value
@AutoConfiguration
@EnableConfigurationProperties(HiveMQEmbeddedProperties.class)
public class HiveMQEmbeddedAutoConfiguration {

    HiveMQEmbeddedProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public HiveMQEmbeddedService hiveMQEmbeddedService() {
        return new HiveMQEmbeddedService(this.properties);
    }
}
