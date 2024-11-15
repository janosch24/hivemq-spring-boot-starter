package com.example.boot.hivemq.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.hivemq.embedded.EmbeddedExtension;
import com.hivemq.embedded.EmbeddedHiveMQ;
import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.annotations.Nullable;
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

        int priority =
                extensions.stream()
                        .map(EmbeddedExtension::getPriority)
                        .max(Comparator.nullsFirst(Integer::compare))
                        .orElse(0);

        int startPriority =
                extensions.stream()
                        .map(EmbeddedExtension::getStartPriority)
                        .max(Comparator.nullsFirst(Integer::compare))
                        .orElse(1000);

        EmbeddedExtensionsCollector embeddedExtensionsWrapper =
                new EmbeddedExtensionsCollector(
                        extensions.stream()
                                  .map(EmbeddedExtensionWrapper::wrap)
                                  .sorted(Comparator.comparing(EmbeddedExtension::getStartPriority).reversed())
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
                                        .withPriority(priority)
                                        .withStartPriority(startPriority)
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
    private static class EmbeddedExtensionsCollector implements ExtensionMain {

        List<EmbeddedExtensionWrapper> extensions;
        HiveMQEmbeddedProperties.Extensions.PublishInfo publishInfo;

        @Override
        public void extensionStart(@NotNull ExtensionStartInput extensionStartInput, @NotNull ExtensionStartOutput extensionStartOutput) {
            this.extensions.forEach(extension -> {
                try {
                    extension.getExtensionMain().extensionStart(extensionStartInput, extensionStartOutput);
                    extension.setStatus(Status.started);
                }
                catch(Throwable th) {
                    log.error("Extension with id {} failed during startup.", extension.getId(), th);
                    extension.setStatus(Status.failed);
                }
            });

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
                                    .forEach(info ->
                                            Services.publishService()
                                                    .publish(Builders.retainedPublish()
                                                            .topic(String.join("/", this.publishInfo.getTopic(),
                                                                    String.valueOf(this.extensions.indexOf(info))))
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
            this.extensions.forEach(extension -> {
                try {
                    extension.getExtensionMain().extensionStop(extensionStopInput, extensionStopOutput);
                }
                catch(Throwable th) {
                    log.error("Extension with id {} failed during shutdown.", extension.getId(), th);
                }
            });
        }
    }

    @Data
    @RequiredArgsConstructor(staticName = "wrap")
    @JsonPropertyOrder({ "status", "name", "id", "version", "author", "startPriority", "priority" })
    private static class EmbeddedExtensionWrapper implements EmbeddedExtension {

        private static final ObjectMapper mapper = new ObjectMapper();

        @JsonIgnore
        private final EmbeddedExtension delegate;

        private Status status = Status.loaded;

        @Override
        public @NotNull String getId() {
            return this.delegate.getId();
        }

        @Override
        public @NotNull String getName() {
            return this.delegate.getName();
        }

        @Override
        public @NotNull String getVersion() {
            return this.delegate.getVersion();
        }

        @Override
        public @Nullable String getAuthor() {
            return this.delegate.getAuthor();
        }

        @Override
        public int getPriority() {
            return this.delegate.getPriority();
        }

        @Override
        public int getStartPriority() {
            return this.delegate.getStartPriority();
        }

        @Override
        @JsonIgnore
        public @NotNull ExtensionMain getExtensionMain() {
            return this.delegate.getExtensionMain();
        }

        @SneakyThrows
        public String jsonify() {
            return mapper.writeValueAsString(this);
        }
    }

    private enum Status {
        loaded, started, failed
    }
}
