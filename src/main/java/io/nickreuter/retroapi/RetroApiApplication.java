package io.nickreuter.retroapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RetroApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(RetroApiApplication.class, args);
	}

}
