# hivemq-spring-boot-starter
A starter to incorporate _HiveMQ-CE_ into _Spring Boot_.

<!-- TOC -->
* [hivemq-spring-boot-starter](#hivemq-spring-boot-starter)
  * [Building](#building)
  * [Usage](#usage)
    * [Gradle](#gradle)
    * [Configuration](#configuration)
  * [Logging](#logging)
  * [Embedded Extensions](#embedded-extensions)
<!-- TOC -->

## Building
This starter is only demo and not (yet) published to _Maven central_.
In order to use it, you have to clone this repository and build it from scratch.
To do so, _cd_ into the project's root and call:

~~~gradle
gradlew publish
~~~

This will build the starter and publish it to your local _Maven_ repository.

## Usage
To use _HiveMQ_ there's no additional code in your application required.
You just have to customize your build script and (if required) your application configuration.

### Gradle
Upon the usual _Spring Boot_ starter, you have to use following dependency in your _build.gradle_ script
in order to embed _HiveMQ_ into your _Spring Boot_ application:

~~~build.gradle
dependencies {
    implementation 'com.example.hivemq.boot:hivemq-spring-boot-starter:2024.7'
}
~~~

As the starter is only published locally, 
you should also include your local _Maven_ repository in your applications build script:

~~~build.gradle
reporitories {
    mavenLocal()
}
~~~

It is also recommended to put an appropriate version entry into your application's _MANIFEST_,
so _HiveMQ_ can find its version on startup and make an appropriate log entry.

~~~build.gradle
tasks.named("bootJar") {
	manifest {
		attributes 'HiveMQ-Version': '2024.7'
	}
}
~~~

_HiveMQ_ uses _javax_ style XML-parsing for config file reading. 
This clashes with _Spring Boot_ managed dependencies for _JAXB_.
In order to make it work, you have to 'downgrade' some dependencies.
You can do this by employing the _Spring Boot_ dependency management plugin in your _build.gradle_:

~~~build.gradle
plugins {
    id 'io.spring.dependency-management' version 1.1.6
} 
~~~

Then put following entries into your project's _gradle.properties_ file:

~~~gradle.properties
jakarta-activation.version = 1.2.2
jakarta-xml-bind.version = 2.3.3
glassfish-jaxb.version = 2.3.9
~~~

### Configuration
This starter in conjunction with _HiveMQ_ comes with sensible default values.
There is no configuration required at all, in order to have default behavior incorporated into 
a _Spring Boot_ application. Nevertheless, all configuration parameters, required by _HiveMQ_, can be customized.  
For a general understanding: This starter consumes _Spring Boot_ style configurations (either properties or yaml)
and streamlines a standard _HiveMQ_ XML-based configuration file upon startup, so _HiveMQ_ can find it.
  
To customize configuration, all values must be prefixed by _hivemq_. For a full set of properties, see example below.
Following table lists all possible values:

| Property                          | mandatory | default                       | description                                                                                                                                                                              |
|-----------------------------------|-----------|-------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| hivemq.enabled                    | no        | true                          | Whether to enable _HiveMQ_                                                                                                                                                               |
| hivemq.auto-start                 | no        | true                          | Whether to automatically start _HiveMQ_ on application startup                                                                                                                           |
| hivemq.config.folder              | no        | .hivemq/conf                  | _HiveMQ_ configuration folder                                                                                                                                                            |
| extensions.folder                 | no        | .hivemq/extensions            | _HiveMQ_ extensions folder                                                                                                                                                               |
| extensions.collector.enabled      | no        | true                          | Whether to collect embedded extensions, see section below                                                                                                                                |
| extensions.collector.info.publish | no        | true                          | Whether to publish info regarding embedded extensions on startup                                                                                                                         |
| extensions.collector.info.topic   | no        | boot/extensions               | MQTT topic to publish info to                                                                                                                                                            |
| log.folder                        | no        | .hivemq/log                   | _HiveMQ_ logging folder, for logging see section below                                                                                                                                   |
| log.level                         | no        | info                          | one of error, warn, info, debug, trace                                                                                                                                                   |
| config.listeners                  | no        | one tcp-listener 0.0.0.0/1883 | Listener configuration follows [_HiveMQ Community Edition_ listener configuration](https://github.com/hivemq/hivemq-community-edition/wiki/Listener-configuration)                       |
| config.mqtt                       | no        | defined by _HiveMQ_           | MQTT related configuration follows [_HiveMQ Community Edition_ MQTT specific configuration](https://github.com/hivemq/hivemq-community-edition/wiki/MQTT-Specific-Configuration)         |
| config.persistence                | no        | defined by _HiveMQ_           | Persistence configuration follows [_HiveMQ Community Edition_ persistence specific configuration](https://github.com/hivemq/hivemq-community-edition/wiki/MQTT-Specific-Configuration)                                |
| config.security                   | no        | defined by _HiveMQ_           | Security related configuration follows [_HiveMQ Community Edition_ security specific configuration](https://github.com/hivemq/hivemq-community-edition/wiki/MQTT-Specific-Configuration) |

Following example gives a full set of available configuration properties:

~~~application.yaml
hivemq:
  enabled: true
  auto-start: true
  config.folder: .hivemq/conf
  data.folder: .hivemq/data
  extensions.folder: .hivemq/extensions
  extensions.collector.enabled: true
  extensions.collector.info.publish: true
  extensions.collector.info.topic: "boot/extensions"
  log.folder: .hivemq/log
  log.level: info

  config:
    listeners:
      tcp-listeners:
        - name: first-tcp-listener
          port: 1883
          bind-address: 0.0.0.0
        - name: second-tcp-listener
          port: 1884
          bind-address: localhost

      websocket-listeners:
        - name: default-websocket-listener
          port: 8000
          bind-address: 0.0.0.0
          path: /mqtt
          allow-extensions: true
          subprotocols: mqttv3.1, mqtt

      tls-tcp-listeners:
        - name: default-tls-tcp-listener
          port: 8883
          bind-address: 0.0.0.0
          tls:
            protocols: TLSv1.1, TLSv1.2
            cipher-suites:
              - TLS_RSA_WITH_AES_128_GCM_SHA256
              - TLS_RSA_WITH_AES_128_CBC_SHA
              - TLS_RSA_WITH_AES_256_CBC_SHA
            client-authentication-mode: none
            handshake-timeout: 10000
            concurrentHandshakeLimit: -1
            native-ssl: false
            keystore:
              path: path-to-keystore
              password: my-secret-password
              private-key-password: my-super-secret-password
            truststore:
              path: path-to-truststore
              password: my-secret-password

      tls-websocket-listeners:
        - name: default-tls-websocket-listener
          port: 8080
          bind-address: 0.0.0.0
          path: /mqtt
          allow-extensions: true
          subprotocols: mqttv3.1, mqtt
          tls:
            protocols:
              - TLSv1.1
              - TLSv1.2
            cipher-suites: TLS_RSA_WITH_AES_128_GCM_SHA256, TLS_RSA_WITH_AES_128_CBC_SHA, TLS_RSA_WITH_AES_256_CBC_SHA
            client-authentication-mode: none
            handshake-timeout: 10000
            concurrentHandshakeLimit: -1
            native-ssl: false
            keystore:
              path: path-to-keystore
              password: my-secret-password
              private-key-password: my-super-secret-password
            truststore:
              path: path-to-truststore
              password: my-secret-password

    mqtt:
      keep-alive.allow-unlimited: false
      keep-alive.max-keep-alive: 65535
      message-expiry.max-interval: 3600
      packets.max-packet-size: 268435460
      quality-of-service.max-qos: 2
      queued-messages.max-queue-size: 1000
      queued-messages.strategy: discard-oldest
      receive-maximum.server-receive-maximum: 10
      retained-messages.enabled: true
      session-expiry.max-interval: 60
      shared-subscriptions.enabled: true
      subscription-identifier.enabled: true
      topic-alias.enabled: true
      topic-alias.max-per-client: 5
      wildcard-subscriptions.enabled: true

    persistence:
      mode: in-memory

    security:
      allow-empty-client-id.enabled: false
      payload-format-validation.enabled: false
      utf8-validation.enabled: true
      allow-request-problem-information.enabled: true
~~~
> **NOTE:** Given values for _HiveMQ_ related configurations are just examples, for available value ranges and
> default values consult the [_HiveMQ Community Edition Wiki_](https://github.com/hivemq/hivemq-community-edition/wiki)

## Logging
_HiveMQ_ is shipped with a _logback.xml_ for logging. _Spring Boot_ detects and uses it unless you define your own 
_logback.xml_. If so, in turn you cannot use _HiveMQ_s _logback.xml_, as it is not ready to be included in your own 
logging configuration. Further, if your application already uses a _Spring_ flavoured _logback-spring.xml_ configuration, 
it will be ignored, as the one from _HiveMQ_ takes precedence.
Therefore, this starter is equipped with a _logback_ configuration, which you can include in your own _logback_ configuration.
It is mainly derived from _HiveMQ_s original _logback.xml_, but adds some _Spring Boot_ specific stuff.
You can use it from your _logback-spring.xml_ as shown in the following example:

~~~logback-spring.xml
<configuration>

    <include resource="logback-spring-hivemq.xml"/>

    <property name="DEFAULT_PATTERN"
              value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%-30.30thread] %-5level %-40(%class{35}:%L) - %msg%n%ex" />

</configuration>
~~~
> ** NOTE:** If you define a _DEFAULT_PATTERN_, _HiveMQ_ logging will follow this, uses its predefined pattern otherwise.

In order to avoid clashes between your own _logback-spring.xml_ and _HiveMQ_s internal configuration it is recommended to
force _Spring Boot_ to use your version of the file _logback-spring.xml_:

~~~application.yaml
logging:
  config: classpath:logback-spring.xml
~~~

## Embedded Extensions
Using this starter you can use embedded extensions, as described in [_HiveMQ Community Edition Quickstart guide_](https://github.com/hivemq/hivemq-community-edition).
As embedded _HiveMQ_ supports only a single embedded extension, this starter provides a convenience wrapper which collects
all available embedded extensions of type _com.hivemq.embedded.EmbeddedExtension_ into a single extension.  
Note, that both, _priority_ and _start priority_ of that single wrapper-extension, will be the maximum values of all
collected extensions. Nevertheless, all collected embedded extensions are sorted by _start priority_ (highest comes first).  
In order to use your own embedded extension, you simply have to create a bean either way _Spring Boot_ recommends it.

~~~java
@Bean
public EmbeddedExtension myEmbeddedExtension() {
    return EmbeddedExtension.builder()
            .withId("my-embedded-extension")
            .withName("My Embedded Extension")
            .withVersion("1.0.0")
            .withPriority(0)
            .withStartPriority(1000)
            .withAuthor("Me")
            .withExtensionMain(new MyEmbeddedExtensionMain())
            .build();
}
~~~

You can also define your embedded extension as a component, for instance:

~~~java
@Component
public final class MyEmbeddedExtension implements EmbeddedExtension, ExtensionMain {

  @Override
  public @NotNull String getId() { return "my-embedded-extension"; }

  @Override
  public @NotNull String getName() { return "My Embedded Extension"; }

  // ...

  @Override
  public @NotNull ExtensionMain getExtensionMain() { return this; }

  // ...
}
~~~
