package com.example.boot.hivemq.config;

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

    @NotNull
    private Folder data = new Folder(defaultDataFolder);

    @NotNull
    private Extensions extensions = new Extensions();

    @NotNull
    private HiveMQ config;

    @Value
    @Validated
    public static class Folder {

        @NotBlank
        String folder;
    }

    @Data
    @Validated
    public static class Extensions {

        @NotBlank
        private String folder = defaultExtensionsFolder;

        private PublishInfo publishInfo = new PublishInfo();

        @Data
        @Validated
        public static class PublishInfo {

            private boolean enabled = true;

            @NotBlank
            private String topic = "$SYS/extensions";
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

        @NotBlank
        @JsonIgnore
        private String folder = ".hivemq/conf";

        @NotNull
        private final Listeners listeners;

        private final Mqtt mqtt;

        private final Security security;

        private final Persistence persistence;
    }

    @Value
    @Validated
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class Listeners {

        @JsonProperty("tcp-listener")
        @JacksonXmlElementWrapper(useWrapping = false)
        List<TcpListener> tcpListeners;

        @JsonProperty("tls-tcp-listener")
        @JacksonXmlElementWrapper(useWrapping = false)
        List<SecureTcpListener> secureTcpListeners;

        @JsonProperty("websocket-listener")
        @JacksonXmlElementWrapper(useWrapping = false)
        List<WebsocketListener> websocketListeners;

        @JsonProperty("tls-websocket-listener")
        @JacksonXmlElementWrapper(useWrapping = false)
        List<SecureWebsocketListener> secureWebsocketListeners;
    }

    @Value
    @Validated
    public static class TcpListener {

        String name;

        @Min(1025)
        @Max(65535)
        Integer port;

        @NotBlank
        @JsonProperty("bind-address")
        String bindAddress;
    }

    @Value
    @Validated
    public static class SecureTcpListener {

        String name;

        @Min(1025)
        @Max(65535)
        Integer port;

        @NotBlank
        @JsonProperty("bind-address")
        String bindAddress;

        @NotNull
        TLS tls;
    }

    @Value
    @Validated
    public static class WebsocketListener {

        String name;

        @Min(1024)
        @Max(65535)
        Integer port;

        @NotBlank
        @JsonProperty("bind-address")
        String bindAddress;

        @NotBlank
        String path;

        @JsonProperty("allow-extensions")
        Boolean allowExtensions;

        @JacksonXmlElementWrapper(localName = "subprotocols")
        @JsonProperty("subprotocol")
        List<String> subprotocols;
    }

    @Value
    @Validated
    public static class SecureWebsocketListener {

        String name;

        @Min(1024)
        @Max(65535)
        Integer port;

        @NotBlank
        @JsonProperty("bind-address")
        String bindAddress;

        @NotBlank
        String path;

        @JsonProperty("allow-extensions")
        Boolean allowExtensions;

        @JacksonXmlElementWrapper(localName = "subprotocols")
        @JsonProperty("subprotocol")
        List<String> subprotocols;

        @NotNull
        TLS tls;
    }


    @Value
    @Validated
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class Mqtt {

        @JsonProperty("session-expiry")
        Expiry sessionExpiry;

        @JsonProperty("message-expiry")
        Expiry messageExpiry;

        @JsonProperty("packets")
        Packets packets;

        @JsonProperty("receive-maximum")
        ReceiveMaximum receiveMaximum;

        @JsonProperty("keep-alive")
        KeepAlive keepAlive;

        @JsonProperty("topic-alias")
        TopicAlias topicAlias;

        @JsonProperty("subscription-identifier")
        MaybeEnabled subscriptionIdentifier;

        @JsonProperty("wildcard-subscriptions")
        MaybeEnabled wildcardSubscriptions;

        @JsonProperty("shared-subscriptions")
        MaybeEnabled sharedSubscriptions;

        @JsonProperty("retained-messages")
        MaybeEnabled retainedMessages;

        @JsonProperty("quality-of-service")
        QualityOfService qualityOfService;

        @JsonProperty("queued-messages")
        QueuedMessages queuedMessages;

        @Value
        @Validated
        public static class Expiry {

            @Min(0)
            @Max(4_294_967_296L)
            @JsonProperty("max-interval")
            Long maxInterval;
        }

        @Value
        @Validated
        public static class Packets {

            @Min(1)
            @Max(268_435_460L)
            @JsonProperty("max-packet-size")
            Long maxPacketSize;
        }

        @Value
        @Validated
        public static class ReceiveMaximum {

            @Min(1)
            @Max(4_294_967_296L)
            @JsonProperty("server-receive-maximum")
            Long serverReceiveMaximum;
        }

        @Value
        @Validated
        public static class KeepAlive {

            @Min(1)
            @Max(4_294_967_296L)
            @JsonProperty("max-keep-alive")
            Long maxKeepAlive;

            @JsonProperty("allow-unlimited")
            Boolean allowUnlimited;
        }

        @Value
        @Validated
        public static class TopicAlias {

            @Min(1)
            @Max(65535L)
            @JsonProperty("max-per-client")
            Long maxPerClient;

            @JsonProperty("enabled")
            Boolean enabled;
        }

        @Value
        @Validated
        public static class QualityOfService {

            @Min(0)
            @Max(2L)
            @JsonProperty("max-qos")
            Long maxQos;
        }

        @Value
        @Validated
        public static class QueuedMessages {

            @Min(1)
            @Max(4_294_967_296L)
            @JsonProperty("max-queue-size")
            Long maxQueueSize;

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

        @NotNull
        @JsonProperty("enabled")
        Boolean enabled;
    }

    @Value
    @Validated
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class Security {

        @JsonProperty("allow-empty-client-id")
        MaybeEnabled allowEmptyClientId;

        @JsonProperty("payload-format-validation")
        MaybeEnabled payloadFormatValidation;

        @JsonProperty("utf8-validation")
        MaybeEnabled utf8Validation;

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

        @NotNull
        Mode mode;
    }

    @Value
    @Validated
    public static class TLS {

        @JacksonXmlElementWrapper(localName = "protocols")
        List<String> protocol;

        @JacksonXmlElementWrapper(localName = "cipher-suites")
        @JsonProperty("cipher-suite")
        List<String> cipherSuite;

        @JsonProperty("client-authentication-mode")
        ClientAuthenticationMode clientAuthenticationMode;

        @JsonProperty("handshake-timeout")
        Integer handshakeTimeout;

        @NotNull
        KeyStore keystore;

        TrustStore truststore;

        @JsonProperty("concurrent-handshake-limit")
        Integer concurrentHandshakeLimit;

        @JsonProperty("native-ssl")
        Boolean nativeSSL;

        @Value
        @Validated
        public static class KeyStore {

            @NotBlank
            String path;

            @NotBlank
            String password;

            @JsonProperty("private-key-password")
            String privateKeyPassword;
        }

        @Value
        @Validated
        public static class TrustStore {

            @NotBlank
            String path;

            @NotBlank
            String password;
        }

        public enum ClientAuthenticationMode {
            NONE, OPTIONAL, REQUIRED
        }
    }
}
