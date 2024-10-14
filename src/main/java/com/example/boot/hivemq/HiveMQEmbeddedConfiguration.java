package com.example.boot.hivemq;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.validation.constraints.*;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Value
@Validated
@ConfigurationProperties("hivemq-ce-embedded")
public class HiveMQEmbeddedConfiguration {

    @NotBlank
    @JsonIgnore
    String logFolder;

    @NotBlank
    @JsonIgnore
    String dataFolder;

    @NotBlank
    @JsonIgnore
    String configFolder;

    @NotBlank
    @JsonIgnore
    String extensionsFolder;

    @NotNull
    @JsonProperty("hivemq")
    HiveMQ hivemq;

    @Value
    @Validated
    @JsonRootName(value = "hivemq")
    private static class HiveMQ {

        @JacksonXmlProperty(isAttribute = true, localName = "xmlns:xsi")
        String nameSpace = "http://www.w3.org/2001/XMLSchema-instance";
        @JacksonXmlProperty(isAttribute = true, localName = "xsi:noNamespaceSchemaLocation")
        String schemaLocation = "hivemq-config.xsd";

        @NotNull
        Listeners listeners;

        Mqtt mqtt;
        Security security;
        Persistence persistence;
    }

    @Value
    @Validated
    private static class Listeners {

        @JsonProperty("tcp-listener")
        @JacksonXmlElementWrapper(useWrapping = false)
        List<TcpListener> tcpListeners;

        @JsonProperty("websocket-listener")
        @JacksonXmlElementWrapper(useWrapping = false)
        List<WebsocketListener> websocketListeners;
    }

    @Value
    @Validated
    private static class TcpListener {

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
    private static class WebsocketListener {

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

        @JsonProperty("subprotocols")
        SubProtocols subProtocols;

        @Value
        @Validated
        private static class SubProtocols {

            @JacksonXmlElementWrapper(useWrapping = false)
            List<String> subprotocol;
        }
    }


    @Value
    @Validated
    private static class Mqtt {

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
        private static class Expiry {

            @Min(0)
            @Max(4_294_967_296L)
            @JsonProperty("max-interval")
            Long maxInterval;
        }

        @Value
        @Validated
        private static class Packets {

            @Min(1)
            @Max(268_435_460L)
            @JsonProperty("max-packet-size")
            Long maxPacketSize;
        }

        @Value
        @Validated
        private static class ReceiveMaximum {

            @Min(1)
            @Max(4_294_967_296L)
            @JsonProperty("server-receive-maximum")
            Long serverReceiveMaximum;
        }

        @Value
        @Validated
        private static class KeepAlive {

            @Min(1)
            @Max(4_294_967_296L)
            @JsonProperty("max-keep-alive")
            Long maxKeepAlive;

            @JsonProperty("allow-unlimited")
            Boolean allowUnlimited;
        }

        @Value
        @Validated
        private static class TopicAlias {

            @Min(1)
            @Max(65535L)
            @JsonProperty("max-per-client")
            Long maxPerClient;

            @JsonProperty("enabled")
            Boolean enabled;
        }

        @Value
        @Validated
        private static class QualityOfService {

            @Min(0)
            @Max(2L)
            @JsonProperty("max-qos")
            Long maxQos;
        }

        @Value
        @Validated
        private static class QueuedMessages {

            @Min(1)
            @Max(4_294_967_296L)
            @JsonProperty("max-queue-size")
            Long maxQueueSize;

            @JsonProperty("strategy")
            Strategy strategy;

            private enum Strategy {

                @JsonProperty("discard")
                discard,

                @JsonProperty("discard-oldest")
                discardOldest
            }
        }
    }

    @Value
    @Validated
    private static class MaybeEnabled {

        @JsonProperty("enabled")
        Boolean enabled;
    }

    @Value
    @Validated
    private static class Security {

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
    private static class Persistence {

        private enum Mode {

            @JsonProperty("in-memory")
            inMemory
        }

        @NotNull
        Mode mode;
    }
}
