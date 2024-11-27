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
package com.example.hivemq.boot.starter.services;

import com.example.hivemq.boot.starter.config.HiveMQEmbeddedAutoConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HiveMQEmbeddedServiceTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(HiveMQEmbeddedAutoConfiguration.class, BuildProperties.class));


    ApplicationContextRunner customize(ApplicationContextRunner contextRunner, Collection<String> properties) {
        return contextRunner.withPropertyValues(properties.toArray(String[]::new));
    }

    @BeforeEach
    void setUp() {}

    @AfterEach
    void tearDown() {}

    @Test
    void shouldContainNoHiveMQBeansAtAll() {
        this.contextRunner
                .with(c -> customize(c, List.of("hivemq.enabled:false")))
                .run(context -> {
                    assertFalse(context.containsBean("hiveMQEmbeddedExtensionsCollector"));
                    assertFalse(context.containsBean("hiveMQEmbeddedServiceWithExtensions"));
                    assertFalse(context.containsBean("hiveMQEmbeddedServiceWithoutExtensions"));
                });
    }

    @Test
    void shouldContainHiveMQWithExtensionsBean() {
        this.contextRunner
                .with(c -> customize(c, List.of("hivemq.auto-start:false")))
                .run(context -> {
                    assertTrue(context.containsBean("hiveMQEmbeddedExtensionsCollector"));
                    assertTrue(context.containsBean("hiveMQEmbeddedServiceWithExtensions"));
                    assertFalse(context.containsBean("hiveMQEmbeddedServiceWithoutExtensions"));
        });
    }

    @Test
    void shouldContainHiveMQWithoutExtensionsBean() {
        this.contextRunner
                .with(c -> customize(c,
                        List.of("hivemq.auto-start:false",
                                "hivemq.extensions.collector.enabled:false")))
                .run(context -> {
                    assertFalse(context.containsBean("hiveMQEmbeddedExtensionsCollector"));
                    assertFalse(context.containsBean("hiveMQEmbeddedServiceWithExtensions"));
                    assertTrue(context.containsBean("hiveMQEmbeddedServiceWithoutExtensions"));
        });
    }

    @Test
    void testAutoStart() {
        this.contextRunner
                .with(c -> customize(c,
                        List.of("hivemq.extensions.collector.enabled:false")))
                .run(context -> {
                    assertTrue(context.getBean(HiveMQEmbeddedService.class).isRunning());
                });
    }

    @Test
    void testManualStart() {
        this.contextRunner
                .with(c -> customize(c,
                        List.of("hivemq.auto-start:false",
                                "hivemq.extensions.collector.enabled:false")))
                .run(context -> {
                    HiveMQEmbeddedService service =
                            context.getBean(HiveMQEmbeddedService.class);

                    assertFalse(service.isRunning());

                    service.startup();
                    assertTrue(service.isRunning());

                    service.shutdown();
                    assertFalse(service.isRunning());
                });
    }
}