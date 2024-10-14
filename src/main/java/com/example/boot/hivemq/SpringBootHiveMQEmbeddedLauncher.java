package com.example.boot.hivemq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.hivemq.embedded.EmbeddedHiveMQ;
import jakarta.annotation.PreDestroy;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(HiveMQEmbeddedConfiguration.class)
public class SpringBootHiveMQEmbeddedLauncher {

	private final EmbeddedHiveMQ embeddedHiveMQ;

	public SpringBootHiveMQEmbeddedLauncher(HiveMQEmbeddedConfiguration configuration) {

		prepareEnvironment(configuration);

		this.embeddedHiveMQ =
				EmbeddedHiveMQ.builder()
						.withConfigurationFolder(Path.of(configuration.getConfigFolder()).toAbsolutePath())
						.withDataFolder(Path.of(configuration.getDataFolder()).toAbsolutePath())
						.withExtensionsFolder(Path.of(configuration.getExtensionsFolder()).toAbsolutePath())
						.withoutLoggingBootstrap()
						.build();
	}

	@SneakyThrows({IOException.class, JsonProcessingException.class})
	private void prepareEnvironment(HiveMQEmbeddedConfiguration configuration) {

		if (!Files.isDirectory(Path.of(configuration.getConfigFolder()).toAbsolutePath())) {
			Files.createDirectories(Path.of(configuration.getConfigFolder()).toAbsolutePath());
		}

		if (!Files.isDirectory(Path.of(configuration.getDataFolder()).toAbsolutePath())) {
			Files.createDirectories(Path.of(configuration.getDataFolder()).toAbsolutePath());
		}

		if (!Files.isDirectory(Path.of(configuration.getLogFolder()).toAbsolutePath())) {
			Files.createDirectories(Path.of(configuration.getLogFolder()).toAbsolutePath());
		}

		if (!Files.isDirectory(Path.of(configuration.getExtensionsFolder()).toAbsolutePath())) {
			Files.createDirectories(Path.of(configuration.getExtensionsFolder()).toAbsolutePath());
		}

		if (Files.isRegularFile(Path.of(configuration.getConfigFolder(), "config.xml")))
			Files.delete(Path.of(configuration.getConfigFolder(), "config.xml"));

		XmlMapper.xmlBuilder()
				.enable(SerializationFeature.INDENT_OUTPUT)
				.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
				.build()
				.writeValue(
						Files.createFile(Path.of(configuration.getConfigFolder(), "config.xml").toAbsolutePath()).toFile(),
						configuration.getHivemq());
	}

	@Bean
	public CommandLineRunner startup(ApplicationContext ctx) {

		return args -> this.embeddedHiveMQ.start().join();
	}

	@PreDestroy
	public void shutdown() {

		this.embeddedHiveMQ.stop().join();
	}

	public static void main(String[] args) {

		SpringApplication.run(SpringBootHiveMQEmbeddedLauncher.class, args);
	}
}
