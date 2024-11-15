package com.example.boot.hivemq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.hivemq.embedded.EmbeddedExtension;
import com.hivemq.embedded.EmbeddedHiveMQ;
import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.packets.general.Qos;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartOutput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopOutput;
import com.hivemq.extension.sdk.api.services.Services;
import com.hivemq.extension.sdk.api.services.admin.LifecycleStage;
import com.hivemq.extension.sdk.api.services.builder.Builders;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HiveMQEmbeddedService {

    private final EmbeddedHiveMQ embeddedHiveMQ;


    HiveMQEmbeddedService(HiveMQEmbeddedProperties properties, List<EmbeddedExtension> extensions) {

        prepareEnvironment(properties);

        EmbeddedExtensionsWrapper embeddedExtensionsWrapper =
                new EmbeddedExtensionsWrapper(
                        extensions.stream()
                                  .sorted(Comparator.comparing(EmbeddedExtension::getStartPriority))
                                  .toList(),
                        properties.getExtensions().getPublishInfo());

        this.embeddedHiveMQ =
                EmbeddedHiveMQ.builder()
                        .withConfigurationFolder(Path.of(properties.getConfig().getFolder()).toAbsolutePath())
                        .withDataFolder(Path.of(properties.getData().getFolder()).toAbsolutePath())
                        .withExtensionsFolder(Path.of(properties.getExtensions().getFolder()).toAbsolutePath())
                        .withEmbeddedExtension(
                                EmbeddedExtension.builder()
                                        .withId("embedded-extension-wrapper")
                                        .withName("Springboot-EmbeddedHiveMQ Extensions Wrapper")
                                        .withAuthor("arajan")
                                        .withVersion("1.0.0")
                                        .withPriority(embeddedExtensionsWrapper.getExtensions()
                                                .stream()
                                                .findFirst()
                                                .map(EmbeddedExtension::getPriority)
                                                .orElse(0))
                                        .withStartPriority(embeddedExtensionsWrapper.getExtensions()
                                                .stream()
                                                .findFirst()
                                                .map(EmbeddedExtension::getStartPriority)
                                                .orElse(0))
                                        .withExtensionMain(embeddedExtensionsWrapper)
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

    @Value
    private static class EmbeddedExtensionsWrapper implements ExtensionMain {

        List<EmbeddedExtension> extensions;
        HiveMQEmbeddedProperties.Extensions.PublishInfo publishInfo;

        @Override
        public void extensionStart(@NotNull ExtensionStartInput extensionStartInput, @NotNull ExtensionStartOutput extensionStartOutput) {
            this.extensions.forEach(ext -> ext.getExtensionMain().extensionStart(extensionStartInput, extensionStartOutput));

            // Additional publish info if required ...
            if (this.publishInfo.isEnabled()) {
                publishInfo();
            }
        }

        private void publishInfo() {

            // Await startup completion and then start everything ...
            Services.extensionExecutorService()
                    .schedule(() -> {
                        // Check if broker is ready
                        if (Services.adminService().getCurrentStage() == LifecycleStage.STARTED_SUCCESSFULLY) {
                            this.extensions
                                    .stream()
                                    .map(Info::of)
                                    .forEach(info ->
                                            Services.publishService()
                                                    .publish(Builders.retainedPublish()
                                                            .topic(String.join("/", this.publishInfo.getTopic(), info.getId()))
                                                            .payload(ByteBuffer.wrap(info.jsonify().getBytes(StandardCharsets.UTF_8)))
                                                            .qos(Qos.AT_LEAST_ONCE)
                                                            .build()));
                        }
                        else {
                            // Try again later
                            publishInfo();
                        }
                    }, 1, TimeUnit.SECONDS);
        }

        @Override
        public void extensionStop(@NotNull ExtensionStopInput extensionStopInput, @NotNull ExtensionStopOutput extensionStopOutput) {
            this.extensions.forEach(ext -> ext.getExtensionMain().extensionStop(extensionStopInput, extensionStopOutput));
        }
    }

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class Info {

        private static final ObjectMapper mapper = new ObjectMapper();

        String id;
        String name;
        String author;
        String version;

        @SneakyThrows
        public String jsonify() {
            return mapper.writeValueAsString(this);
        }

        public static Info of(EmbeddedExtension extension) {
            return Info.builder()
                    .id(extension.getId())
                    .name(extension.getName())
                    .author(extension.getAuthor())
                    .version(extension.getVersion())
                    .build();
        }
    }
}
