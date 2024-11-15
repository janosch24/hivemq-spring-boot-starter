package com.example.boot.hivemq.config;

import com.hivemq.embedded.EmbeddedExtension;
import lombok.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

@Value
@AutoConfiguration
@ConditionalOnClass(com.hivemq.HiveMQServer.class)
@EnableConfigurationProperties(HiveMQEmbeddedProperties.class)
@ConditionalOnProperty(value = "hivemq.enabled", havingValue = "true", matchIfMissing = true)
public class HiveMQEmbeddedAutoConfiguration {

    HiveMQEmbeddedProperties properties;
    List<EmbeddedExtension> extensions;

    @Bean
    @ConditionalOnMissingBean
    public HiveMQEmbeddedService hiveMQEmbeddedService() {
        return new HiveMQEmbeddedService(this.properties, extensions);
    }
}
