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

import com.example.hivemq.boot.starter.config.HiveMQEmbeddedProperties;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.hivemq.embedded.EmbeddedExtension;
import com.hivemq.embedded.EmbeddedHiveMQ;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public final class HiveMQEmbeddedService {

    private final EmbeddedHiveMQ embeddedHiveMQ;
    private final boolean autoStart;

    @Getter
    private volatile boolean running = false;

    public HiveMQEmbeddedService(HiveMQEmbeddedProperties properties) {

        prepareEnvironment(properties);

        this.embeddedHiveMQ =
                EmbeddedHiveMQ.builder()
                        .withConfigurationFolder(Path.of(properties.getConfig().getFolder()).toAbsolutePath())
                        .withDataFolder(Path.of(properties.getData().getFolder()).toAbsolutePath())
                        .withExtensionsFolder(Path.of(properties.getExtensions().getFolder()).toAbsolutePath())
                        .withoutLoggingBootstrap()
                        .build();

        this.autoStart = properties.isAutoStart();
    }

    public HiveMQEmbeddedService(HiveMQEmbeddedProperties properties,
                                 HiveMQEmbeddedExtensionsCollector extensionsCollector) {

        prepareEnvironment(properties);

        this.embeddedHiveMQ =
                EmbeddedHiveMQ.builder()
                        .withConfigurationFolder(Path.of(properties.getConfig().getFolder()).toAbsolutePath())
                        .withDataFolder(Path.of(properties.getData().getFolder()).toAbsolutePath())
                        .withExtensionsFolder(Path.of(properties.getExtensions().getFolder()).toAbsolutePath())
                        .withEmbeddedExtension(
                                EmbeddedExtension.builder()
                                        .withId(extensionsCollector.getId())
                                        .withName(extensionsCollector.getName())
                                        .withAuthor(extensionsCollector.getAuthor())
                                        .withVersion(extensionsCollector.getVersion())
                                        .withPriority(extensionsCollector.getPriority())
                                        .withStartPriority(extensionsCollector.getStartPriority())
                                        .withExtensionMain(extensionsCollector)
                                        .build())
                        .withoutLoggingBootstrap()
                        .build();

        this.autoStart = properties.isAutoStart();
    }

    @SneakyThrows({ IOException.class })
    private void prepareEnvironment(HiveMQEmbeddedProperties properties) {

        if (!Files.isDirectory(Path.of(properties.getConfig().getFolder()).toAbsolutePath())) {
            Files.createDirectories(Path.of(properties.getConfig().getFolder()).toAbsolutePath());
        }

        if (!Files.isDirectory(Path.of(properties.getData().getFolder()).toAbsolutePath())) {
            Files.createDirectories(Path.of(properties.getData().getFolder()).toAbsolutePath());
        }

        if (!Files.isDirectory(Path.of(properties.getExtensions().getFolder()).toAbsolutePath())) {
            Files.createDirectories(Path.of(properties.getExtensions().getFolder()).toAbsolutePath());
        }

        if (Files.isRegularFile(Path.of(properties.getConfig().getFolder(), "config.xml")))
            Files.delete(Path.of(properties.getConfig().getFolder(), "config.xml"));

        XmlMapper.xmlBuilder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
                .build()
                .writeValue(
                        Files.createFile(Path.of(properties.getConfig().getFolder(), "config.xml").toAbsolutePath()).toFile(),
                        properties.getConfig());
    }

    @PostConstruct
    private void internalStartup() {
        if (this.autoStart)
            startup();
    }

    @Synchronized
    public void startup() {
        this.embeddedHiveMQ.start().join();
        this.running = true;
    }

    @PreDestroy
    @Synchronized
    public void shutdown() {
        this.embeddedHiveMQ.stop().join();
        this.running = false;
    }
}
