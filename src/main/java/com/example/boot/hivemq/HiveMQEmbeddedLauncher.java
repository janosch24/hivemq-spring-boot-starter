package com.example.boot.hivemq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HiveMQEmbeddedLauncher {

	@Bean
	public EmbeddedExtension2 embeddedExtension2() {
		return new EmbeddedExtension2();
	}

	public static void main(String[] args) {
		SpringApplication.run(HiveMQEmbeddedLauncher.class, args);
	}
}
