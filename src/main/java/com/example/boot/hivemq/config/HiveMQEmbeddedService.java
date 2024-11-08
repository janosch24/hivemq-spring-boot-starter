package com.example.boot.hivemq.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.hivemq.embedded.EmbeddedHiveMQ;
import com.hivemq.embedded.EmbeddedHiveMQBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

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
                        .withConfigurationFolder(Path.of(properties.getConfigFolder()).toAbsolutePath())
                        .withDataFolder(Path.of(properties.getDataFolder()).toAbsolutePath())
                        .withExtensionsFolder(Path.of(properties.getExtensionsFolder()).toAbsolutePath())
                        .withoutLoggingBootstrap()
                        .build();
    }

    @SneakyThrows({ IOException.class })
    private void prepareEnvironment(HiveMQEmbeddedProperties properties) {

        if (!Files.isDirectory(Path.of(properties.getConfigFolder()).toAbsolutePath())) {
            Files.createDirectories(Path.of(properties.getConfigFolder()).toAbsolutePath());
        }

        if (!Files.isDirectory(Path.of(properties.getDataFolder()).toAbsolutePath())) {
            Files.createDirectories(Path.of(properties.getDataFolder()).toAbsolutePath());
        }

        if (!Files.isDirectory(Path.of(properties.getLogFolder()).toAbsolutePath())) {
            Files.createDirectories(Path.of(properties.getLogFolder()).toAbsolutePath());
        }

        if (!Files.isDirectory(Path.of(properties.getExtensionsFolder()).toAbsolutePath())) {
            Files.createDirectories(Path.of(properties.getExtensionsFolder()).toAbsolutePath());
        }

        if (Files.isRegularFile(Path.of(properties.getConfigFolder(), "config.xml")))
            Files.delete(Path.of(properties.getConfigFolder(), "config.xml"));

        XmlMapper.xmlBuilder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
                .build()
                .writeValue(
                        Files.createFile(Path.of(properties.getConfigFolder(), "config.xml").toAbsolutePath()).toFile(),
                        properties.getConfig());
    }

//    @Bean
//    public CommandLineRunner startup(ApplicationContext ctx) {
//        return args -> {
//            this.embeddedHiveMQ =
//                    this.embeddedHiveMQBuilder.build();
//            this.embeddedHiveMQ.start().join();
//        };
//    }

    @PostConstruct
    public void startup() {
        this.embeddedHiveMQ.start().join();
    }

    @PreDestroy
    public void shutdown() {
        this.embeddedHiveMQ.stop().join();
    }
}
