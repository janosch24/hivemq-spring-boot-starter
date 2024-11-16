package com.example.boot.hivemq.config;

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
public class HiveMQEmbeddedService {

    private final EmbeddedHiveMQ embeddedHiveMQ;

    HiveMQEmbeddedService(HiveMQEmbeddedProperties properties) {

        prepareEnvironment(properties);

        this.embeddedHiveMQ =
                EmbeddedHiveMQ.builder()
                        .withConfigurationFolder(Path.of(properties.getConfig().getFolder()).toAbsolutePath())
                        .withDataFolder(Path.of(properties.getData().getFolder()).toAbsolutePath())
                        .withExtensionsFolder(Path.of(properties.getExtensions().getFolder()).toAbsolutePath())
                        .withoutLoggingBootstrap()
                        .build();
    }

    HiveMQEmbeddedService(HiveMQEmbeddedProperties properties,
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
    public void startup() {
        this.embeddedHiveMQ.start().join();
    }

    @PreDestroy
    public void shutdown() {
        this.embeddedHiveMQ.stop().join();
    }
}
