package com.example.hivemq.boot.starter.config;

import com.example.hivemq.boot.starter.services.HiveMQEmbeddedExtensionsCollector;
import com.example.hivemq.boot.starter.services.HiveMQEmbeddedService;
import com.hivemq.embedded.EmbeddedExtension;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;

import java.util.Comparator;
import java.util.List;

@Value
@AutoConfiguration
@ConditionalOnClass(com.hivemq.HiveMQServer.class)
@EnableConfigurationProperties(HiveMQEmbeddedProperties.class)
@ConditionalOnProperty(value = "hivemq.enabled", havingValue = "true", matchIfMissing = true)
public class HiveMQEmbeddedAutoConfiguration {

    HiveMQEmbeddedProperties properties;

    @Getter(AccessLevel.NONE)
    List<EmbeddedExtension> extensions;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "hivemq.extensions.collector.enabled", havingValue = "true", matchIfMissing = true)
    public HiveMQEmbeddedExtensionsCollector hiveMQEmbeddedExtensionsCollector(BuildProperties buildProperties) {
        return new HiveMQEmbeddedExtensionsCollector(buildProperties,
                this.properties.getExtensions().getCollector().getPublishInfo(),
                this.extensions.stream()
                        .map(HiveMQEmbeddedExtensionsCollector.HiveMQEmbeddedExtensionWrapper::wrap)
                        .sorted(Comparator.comparing(EmbeddedExtension::getStartPriority).reversed())
                        .toList());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(value = HiveMQEmbeddedExtensionsCollector.class)
    @ConditionalOnProperty(value = "hivemq.extensions.collector.enabled", havingValue = "true", matchIfMissing = true)
    public HiveMQEmbeddedService hiveMQEmbeddedServiceWithExtensions(HiveMQEmbeddedExtensionsCollector extensionsCollector) {
        return new HiveMQEmbeddedService(this.properties, extensionsCollector);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "hivemq.extensions.collector.enabled", havingValue = "false")
    public HiveMQEmbeddedService hiveMQEmbeddedServiceWithoutExtensions() {
        return new HiveMQEmbeddedService(this.properties);
    }
}
