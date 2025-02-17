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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.embedded.EmbeddedExtension;
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
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.info.BuildProperties;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Collects wrapped embedded extensions.
 * As this class implements the required extension methods itself,
 * it can act as a single embedded extension and delegate calls to
 * start and stop from hivemq to all collected embedded extensions.
 */
@Value
@Slf4j
@JsonPropertyOrder({ "id", "name", "version", "author", "startPriority", "priority" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HiveMQEmbeddedExtensionsCollector implements ExtensionMain {

    private static final ObjectMapper mapper =
            new ObjectMapper();

    @JsonIgnore
    BuildProperties buildProperties;

    @JsonIgnore
    HiveMQEmbeddedProperties.Extensions.PublishInfo publishInfo;

    List<HiveMQEmbeddedExtensionWrapper> extensions;

    @Override
    public void extensionStart(@NotNull ExtensionStartInput extensionStartInput,
                               @NotNull ExtensionStartOutput extensionStartOutput) {

        this.extensions.forEach(extension -> {
            try {
                extension.getExtensionMain().extensionStart(extensionStartInput, extensionStartOutput);
                extension.setStatus(Status.started);

                log.info("Embedded extension '{}' version {} started successfully.", extension.getName(), extension.getVersion());
            } catch (Throwable th) {
                log.error("Embedded extension '{}' failed during startup.", extension.getName(), th);
                extension.setStatus(Status.failed);
            }
        });

        // Additional publish info if required ...
        if (this.publishInfo.isPublish()) {
            publishInfo();
        }
    }

    @Override
    public void extensionStop(@NotNull ExtensionStopInput extensionStopInput,
                              @NotNull ExtensionStopOutput extensionStopOutput) {

        this.extensions.forEach(extension -> {
            try {
                extension.getExtensionMain().extensionStop(extensionStopInput, extensionStopOutput);

                log.info("Embedded extension '{}' version {} stopped successfully.", extension.getName(), extension.getVersion());
            } catch (Throwable th) {
                log.error("Embedded extension '{}' failed during shutdown.", extension.getName(), th);
            }
        });
    }

    private void publishInfo() {

        // Await startup completion and then start everything ...
        Services.extensionExecutorService()
                .schedule(() -> {
                    // Check if broker is ready
                    if (Services.adminService().getCurrentStage() == LifecycleStage.STARTED_SUCCESSFULLY) {
                        Services.publishService()
                                .publish(Builders.retainedPublish()
                                            .topic(String.join("/", this.publishInfo.getTopic(), getId()))
                                            .payload(ByteBuffer.wrap(jsonify(this).getBytes(StandardCharsets.UTF_8)))
                                            .qos(Qos.AT_LEAST_ONCE)
                                            .build());
                    } else {
                        // Try again later
                        publishInfo();
                    }
                }, 1, TimeUnit.SECONDS);
    }

    /**
     * @return The extension id
     */
    public @NotNull String getId() {
        return "spring-boot-hivemq-embedded-extensions-collector";
    }

    /**
     * @return The extension name
     */
    public @NotNull String getName() {
        return "Springboot EmbeddedHiveMQ-Extensions Collector";
    }

    /**
     * @return The extension version
     */
    public @NotNull String getVersion() {
        return Optional.ofNullable(this.buildProperties.getVersion())
                .orElse("development");
    }

    /**
     * @return The extension author
     */
    public @Nullable String getAuthor() {
        return null;
    }

    /**
     * @return The extension priority, this is the maximum priority of all collected extensions
     */
    public int getPriority() {
        return this.extensions.stream()
                .map(EmbeddedExtension::getPriority)
                .max(Comparator.nullsFirst(Integer::compare))
                .orElse(0);
    }

    /**
     * @return The extension start priority, this is the maximum start priority of all collected extensions
     */
    public int getStartPriority() {
        return this.extensions.stream()
                .map(EmbeddedExtension::getStartPriority)
                .max(Comparator.nullsFirst(Integer::compare))
                .orElse(1000);
    }

    @SneakyThrows
    private static String jsonify(Object value) {
        return mapper.writeValueAsString(value);
    }

    public enum Status {
        loaded, started, failed
    }

    /**
     * Wrapper class for all HiveMQ extensions
     */
    @Data
    @RequiredArgsConstructor(staticName = "wrap")
    @JsonPropertyOrder({ "status", "id", "name", "version", "author", "startPriority", "priority" })
    public static final class HiveMQEmbeddedExtensionWrapper implements EmbeddedExtension {

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
    }
}
