/*
 *    Copyright 2024-present Jan Haenel
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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

    @Bean("hiveMQEmbeddedExtensionsCollector")
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

    @Bean("hiveMQEmbeddedServiceWithExtensions")
    @ConditionalOnMissingBean(type = "HiveMQEmbeddedService")
    @ConditionalOnBean(value = HiveMQEmbeddedExtensionsCollector.class)
    @ConditionalOnProperty(value = "hivemq.extensions.collector.enabled", havingValue = "true", matchIfMissing = true)
    public HiveMQEmbeddedService hiveMQEmbeddedServiceWithExtensions(HiveMQEmbeddedExtensionsCollector extensionsCollector) {
        return new HiveMQEmbeddedService(this.properties, extensionsCollector);
    }

    @Bean("hiveMQEmbeddedServiceWithoutExtensions")
    @ConditionalOnMissingBean(type = "HiveMQEmbeddedService")
    @ConditionalOnProperty(value = "hivemq.extensions.collector.enabled", havingValue = "false")
    public HiveMQEmbeddedService hiveMQEmbeddedServiceWithoutExtensions() {
        return new HiveMQEmbeddedService(this.properties);
    }
}
