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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@Validated
@ConfigurationProperties("hivemq")
public class HiveMQEmbeddedProperties {

    private static final String defaultConfigFolder = ".hivemq/conf";
    private static final String defaultDataFolder = ".hivemq/data";
    private static final String defaultExtensionsFolder = ".hivemq/extensions";

    /**
     * Whether to enable HiveMQ
     */
    private boolean enabled = true;

    /**
     * Whether to automatically start HiveMQ
     */
    private boolean autoStart = true;

    /**
     * HiveMQ data persistence
     */
    @NotNull
    private Folder data = new Folder(defaultDataFolder);

    /**
     * HiveMQ extensions
     */
    @NotNull
    private Extensions extensions = new Extensions();

    /**
     * HiveMQ configuration
     */
    @NotNull
    private HiveMQ config =
            new HiveMQ(Listeners.defaults(), null, null, null);

    @Value
    @Validated
    public static class Folder {

        /**
         * Storage folder
         */
        @NotBlank
        String folder;
    }

    @Data
    @Validated
    public static class Extensions {

        /**
         * HiveMQ extensions storage folder
         */
        @NotBlank
        private String folder = defaultExtensionsFolder;

        /**
         * HiveMQ collector for Spring Boot managed embedded extensions
         */
        private Collector collector = new Collector();
        @Data
        @Validated
        public static class Collector {

            /**
             * Enable Spring Boot managed embedded extensions
             */
            private boolean enabled = true;

            /**
             * Publish info for Spring Boot managed embedded extensions
             */
            private PublishInfo publishInfo = new PublishInfo();

        }

        @Data
        @Validated
        public static class PublishInfo {

            /**
             * Whether to publish info
             */
            private boolean enabled = true;

            /**
             * Topic to publish info to
             */
            @NotBlank
            private String topic = "boot/extensions";
        }
    }

    @Data
    @Validated
    @JsonRootName(value = "hivemq")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class HiveMQ {

        @JacksonXmlProperty(isAttribute = true, localName = "xmlns:xsi")
        private final String nameSpace = "http://www.w3.org/2001/XMLSchema-instance";
        @JacksonXmlProperty(isAttribute = true, localName = "xsi:noNamespaceSchemaLocation")
        private final String schemaLocation = "hivemq-config.xsd";

        /**
         * HiveMQ configuration folder
         */
        @NotBlank
        @JsonIgnore
        private String folder = ".hivemq/conf";

        /**
         * HiveMQ listener configuration
         */
        @NotNull
        private final Listeners listeners;

        /**
         * HiveMQ mqtt configuration
         */
        private final Mqtt mqtt;

        /**
         * HiveMQ security configuration
         */
        private final Security security;

        /**
         * HiveMQ persistence configuration
         */
        private final Persistence persistence;
    }

    @Value
    @Validated
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class Listeners {

        /**
         * List of HiveMQ tcp listeners
         */
        @JsonProperty("tcp-listener")
        @JacksonXmlElementWrapper(useWrapping = false)
        List<TcpListener> tcpListeners;

        /**
         * List of HiveMQ secure tcp listeners
         */
        @JsonProperty("tls-tcp-listener")
        @JacksonXmlElementWrapper(useWrapping = false)
        List<SecureTcpListener> secureTcpListeners;

        /**
         * List of HiveMQ websocket listeners
         */
        @JsonProperty("websocket-listener")
        @JacksonXmlElementWrapper(useWrapping = false)
        List<WebsocketListener> websocketListeners;

        /**
         * List of HiveMQ secure websocket listeners
         */
        @JsonProperty("tls-websocket-listener")
        @JacksonXmlElementWrapper(useWrapping = false)
        List<SecureWebsocketListener> secureWebsocketListeners;

        public static Listeners defaults() {
            return new Listeners(List.of(TcpListener.defaultListener("0.0.0.0", 1883)),
                    null, null, null);
        }
    }

    @Value
    @Validated
    public static class TcpListener {

        /**
         * Optional listener name
         */
        String name;

        /**
         * Listener bind address
         */
        @NotBlank
        @JsonProperty("bind-address")
        String bindAddress;

        /**
         * Port to listen od
         */
        @Min(1025)
        @Max(65535)
        Integer port;

        public static TcpListener defaultListener(@NotBlank String bindAddress, int port) {
            return new TcpListener(null, bindAddress, port);
        }
    }

    @Value
    @Validated
    public static class SecureTcpListener {

        /**
         * Optional listener name
         */
        String name;

        /**
         * Listener bind address
         */
        @NotBlank
        @JsonProperty("bind-address")
        String bindAddress;

        /**
         * Port to listen on
         */
        @Min(1025)
        @Max(65535)
        Integer port;

        /**
         * TLS configuration
         */
        @NotNull
        TLS tls;
    }

    @Value
    @Validated
    public static class WebsocketListener {

        /**
         * Optional listener name
         */
        String name;

        /**
         * Listener bind address
         */
        @NotBlank
        @JsonProperty("bind-address")
        String bindAddress;

        /**
         * Port to listen on
         */
        @Min(1024)
        @Max(65535)
        Integer port;

        /**
         * Websocket path
         */
        @NotBlank
        String path;

        /**
         * Whether to allow extensions
         */
        @JsonProperty("allow-extensions")
        Boolean allowExtensions;

        /**
         * Comma-separated list of sub-protocols
         */
        @JacksonXmlElementWrapper(localName = "subprotocols")
        @JsonProperty("subprotocol")
        List<String> subprotocols;
    }

    @Value
    @Validated
    public static class SecureWebsocketListener {

        /**
         * Optional listener name
         */
        String name;

        /**
         * Listener bind address
         */
        @NotBlank
        @JsonProperty("bind-address")
        String bindAddress;

        /**
         * Port to listen on
         */
        @Min(1024)
        @Max(65535)
        Integer port;

        /**
         * Websocket path
         */
        @NotBlank
        String path;

        /**
         * Whether to allow extensions
         */
        @JsonProperty("allow-extensions")
        Boolean allowExtensions;

        /**
         * Conmma-separated list of sub-protocols
         */
        @JacksonXmlElementWrapper(localName = "subprotocols")
        @JsonProperty("subprotocol")
        List<String> subprotocols;

        /**
         * TLS configuration
         */
        @NotNull
        TLS tls;
    }


    @Value
    @Validated
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class Mqtt {

        /**
         * Session expiry timeout
         */
        @JsonProperty("session-expiry")
        Expiry sessionExpiry;

        /**
         * Message expiry timeout
         */
        @JsonProperty("message-expiry")
        Expiry messageExpiry;

        /**
         * Mqtt packets configuration
         */
        @JsonProperty("packets")
        Packets packets;

        /**
         * Mqtt receive maximum configuration
         */
        @JsonProperty("receive-maximum")
        ReceiveMaximum receiveMaximum;

        /**
         * Mqtt keep-alive
         */
        @JsonProperty("keep-alive")
        KeepAlive keepAlive;

        /**
         * Mqtt topic alias configuration
         */
        @JsonProperty("topic-alias")
        TopicAlias topicAlias;

        /**
         * Mqtt subscription identifier configuration
         */
        @JsonProperty("subscription-identifier")
        MaybeEnabled subscriptionIdentifier;

        /**
         * Mqtt wildcard subscriptions configuration
         */
        @JsonProperty("wildcard-subscriptions")
        MaybeEnabled wildcardSubscriptions;

        /**
         * Mqtt shared subscriptions configuration
         */
        @JsonProperty("shared-subscriptions")
        MaybeEnabled sharedSubscriptions;

        /**
         * Mqtt retained messages configuration
         */
        @JsonProperty("retained-messages")
        MaybeEnabled retainedMessages;

        /**
         * Mqtt Qos configuration
         */
        @JsonProperty("quality-of-service")
        QualityOfService qualityOfService;

        /**
         * Mqtt message queueing configuration
         */
        @JsonProperty("queued-messages")
        QueuedMessages queuedMessages;

        @Value
        @Validated
        public static class Expiry {

            /**
             * Expiry interval in units of milliseconds
             */
            @Min(0)
            @Max(4_294_967_296L)
            @JsonProperty("max-interval")
            Long maxInterval;
        }

        @Value
        @Validated
        public static class Packets {

            /**
             * Maximum mqtt packet size
             */
            @Min(1)
            @Max(268_435_460L)
            @JsonProperty("max-packet-size")
            Long maxPacketSize;
        }

        @Value
        @Validated
        public static class ReceiveMaximum {

            /**
             * Maximum number of concurrent publishes per client
             */
            @Min(1)
            @Max(4_294_967_296L)
            @JsonProperty("server-receive-maximum")
            Long serverReceiveMaximum;
        }

        @Value
        @Validated
        public static class KeepAlive {

            /**
             * Maximum keep-alive time in milliseconds
             */
            @Min(1)
            @Max(4_294_967_296L)
            @JsonProperty("max-keep-alive")
            Long maxKeepAlive;

            /**
             * Whether to allow unlimited keep-alive
             */
            @JsonProperty("allow-unlimited")
            Boolean allowUnlimited;
        }

        @Value
        @Validated
        public static class TopicAlias {

            /**
             * Maximum topic aliases allowed per client
             */
            @Min(1)
            @Max(65535L)
            @JsonProperty("max-per-client")
            Long maxPerClient;

            /**
             * Whether to allow topic aliases
             */
            @JsonProperty("enabled")
            Boolean enabled;
        }

        @Value
        @Validated
        public static class QualityOfService {

            /**
             * Maximum Qos allowed
             */
            @Min(0)
            @Max(2L)
            @JsonProperty("max-qos")
            Long maxQos;
        }

        @Value
        @Validated
        public static class QueuedMessages {

            /**
             * Maximum number of messages queued
             */
            @Min(1)
            @Max(4_294_967_296L)
            @JsonProperty("max-queue-size")
            Long maxQueueSize;

            /**
             * Queueing strategy
             */
            @JsonProperty("strategy")
            Strategy strategy;

            public enum Strategy {

                @JsonProperty("discard")
                discard,

                @JsonProperty("discard-oldest")
                discardOldest
            }
        }
    }

    @Value
    @Validated
    public static class MaybeEnabled {

        /**
         * Whether to enable this feature
         */
        @NotNull
        @JsonProperty("enabled")
        Boolean enabled;
    }

    @Value
    @Validated
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class Security {

        /**
         * Allow empty client-id configuration
         */
        @JsonProperty("allow-empty-client-id")
        MaybeEnabled allowEmptyClientId;

        /**
         * Payload format validation configuration
         */
        @JsonProperty("payload-format-validation")
        MaybeEnabled payloadFormatValidation;

        /**
         * UTF8 validation configuration
         */
        @JsonProperty("utf8-validation")
        MaybeEnabled utf8Validation;

        /**
         * Allow request problem information configuration
         */
        @JsonProperty("allow-request-problem-information")
        MaybeEnabled allowRequestProblemInformation;
    }

    @Value
    @Validated
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class Persistence {

        public enum Mode {

            @JsonProperty("in-memory")
            inMemory
        }

        /**
         * Persistence mode to use
         */
        @NotNull
        Mode mode;
    }

    @Value
    @Validated
    public static class TLS {

        /**
         * TLS protocol to use
         */
        @JacksonXmlElementWrapper(localName = "protocols")
        List<String> protocol;

        /**
         * Comma-separated list of cipher suites to use
         */
        @JacksonXmlElementWrapper(localName = "cipher-suites")
        @JsonProperty("cipher-suite")
        List<String> cipherSuite;

        /**
         * Client authentication mode to be used
         */
        @JsonProperty("client-authentication-mode")
        ClientAuthenticationMode clientAuthenticationMode;

        /**
         * Handshake timeout in milliseconds
         */
        @JsonProperty("handshake-timeout")
        Integer handshakeTimeout;

        /**
         * Mandatory keystore configuration
         */
        @NotNull
        KeyStore keystore;

        /**
         * Optional truststore configuration
         */
        TrustStore truststore;

        /**
         * The maximum number of SSL handshakes, that can be in progress at any time (set to a positive non-zero integer to activate)
         */
        @JsonProperty("concurrent-handshake-limit")
        Integer concurrentHandshakeLimit;

        /**
         * Whether to use native SSL
         */
        @JsonProperty("native-ssl")
        Boolean nativeSSL;

        @Value
        @Validated
        public static class KeyStore {

            /**
             * Path to keystore
             */
            @NotBlank
            String path;

            /**
             * Keystore password
             */
            @NotBlank
            String password;

            /**
             * Optional private key password
             */
            @JsonProperty("private-key-password")
            String privateKeyPassword;
        }

        @Value
        @Validated
        public static class TrustStore {

            /**
             * Path to truststore
             */
            @NotBlank
            String path;

            /**
             * Truststore password
             */
            @NotBlank
            String password;
        }

        public enum ClientAuthenticationMode {
            NONE, OPTIONAL, REQUIRED
        }
    }
}
