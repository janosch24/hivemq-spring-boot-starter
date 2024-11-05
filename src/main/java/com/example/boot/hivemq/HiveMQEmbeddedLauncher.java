package com.example.boot.hivemq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HiveMQEmbeddedLauncher {

	public static void main(String[] args) {
		SpringApplication.run(HiveMQEmbeddedLauncher.class, args);
	}
}
