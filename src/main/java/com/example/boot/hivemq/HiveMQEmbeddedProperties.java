package com.example.boot.hivemq;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.springframework.lang.Nullable;
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

    @NotBlank
    private String configFolder = ".hivemq/conf";

    @NotBlank
    private String dataFolder = ".hivemq/data";

    @NotBlank
    private String extensionsFolder = ".hivemq/extensions";

    @NotBlank
    private String logFolder = ".hivemq/log";

    @NotNull
    private HiveMQ config;

    @Value
    @Validated
    @JsonRootName(value = "hivemq")
    public static class HiveMQ {

        @JacksonXmlProperty(isAttribute = true, localName = "xmlns:xsi")
        String nameSpace = "http://www.w3.org/2001/XMLSchema-instance";
        @JacksonXmlProperty(isAttribute = true, localName = "xsi:noNamespaceSchemaLocation")
        String schemaLocation = "hivemq-config.xsd";

        @NotNull
        Listeners listeners;

        @Nullable
        Mqtt mqtt;

        @Nullable
        Security security;

        @Nullable
        Persistence persistence;
    }

    @Value
    @Validated
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

        @Nullable
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

        @Nullable
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

        @Nullable
        String name;

        @Min(1024)
        @Max(65535)
        Integer port;

        @NotBlank
        @JsonProperty("bind-address")
        String bindAddress;

        @NotBlank
        String path;

        @Nullable
        @JsonProperty("allow-extensions")
        Boolean allowExtensions;

        @JacksonXmlElementWrapper(localName = "subprotocols")
        @JsonProperty("subprotocol")
        List<String> subprotocols;
    }

    @Value
    @Validated
    public static class SecureWebsocketListener {

        @Nullable
        String name;

        @Min(1024)
        @Max(65535)
        Integer port;

        @NotBlank
        @JsonProperty("bind-address")
        String bindAddress;

        @NotBlank
        String path;

        @Nullable
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
    public static class Mqtt {

        @Nullable
        @JsonProperty("session-expiry")
        Expiry sessionExpiry;

        @Nullable
        @JsonProperty("message-expiry")
        Expiry messageExpiry;

        @Nullable
        @JsonProperty("packets")
        Packets packets;

        @Nullable
        @JsonProperty("receive-maximum")
        ReceiveMaximum receiveMaximum;

        @Nullable
        @JsonProperty("keep-alive")
        KeepAlive keepAlive;

        @Nullable
        @JsonProperty("topic-alias")
        TopicAlias topicAlias;

        @Nullable
        @JsonProperty("subscription-identifier")
        MaybeEnabled subscriptionIdentifier;

        @Nullable
        @JsonProperty("wildcard-subscriptions")
        MaybeEnabled wildcardSubscriptions;

        @Nullable
        @JsonProperty("shared-subscriptions")
        MaybeEnabled sharedSubscriptions;

        @Nullable
        @JsonProperty("retained-messages")
        MaybeEnabled retainedMessages;

        @Nullable
        @JsonProperty("quality-of-service")
        QualityOfService qualityOfService;

        @Nullable
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

            @Nullable
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

            @Nullable
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
    public static class Security {

        @Nullable
        @JsonProperty("allow-empty-client-id")
        MaybeEnabled allowEmptyClientId;

        @Nullable
        @JsonProperty("payload-format-validation")
        MaybeEnabled payloadFormatValidation;

        @Nullable
        @JsonProperty("utf8-validation")
        MaybeEnabled utf8Validation;

        @Nullable
        @JsonProperty("allow-request-problem-information")
        MaybeEnabled allowRequestProblemInformation;
    }

    @Value
    @Validated
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

        @Nullable
        @JacksonXmlElementWrapper(localName = "protocols")
        List<String> protocol;

        @Nullable
        @JacksonXmlElementWrapper(localName = "cipher-suites")
        @JsonProperty("cipher-suite")
        List<String> cipherSuite;

        @Nullable
        @JsonProperty("client-authentication-mode")
        ClientAuthenticationMode clientAuthenticationMode;

        @Nullable
        @JsonProperty("handshake-timeout")
        Integer handshakeTimeout;

        @NotNull
        KeyStore keystore;

        @Nullable
        TrustStore truststore;

        @Nullable
        @JsonProperty("concurrent-handshake-limit")
        Integer concurrentHandshakeLimit;

        @Nullable
        @JsonProperty("native-ssl")
        Boolean nativeSSL;

        @Value
        @Validated
        public static class KeyStore {

            @NotBlank
            String path;

            @NotBlank
            String password;

            @Nullable
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
