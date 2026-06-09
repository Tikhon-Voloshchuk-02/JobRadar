package com.jobradar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@EnableScheduling
@SpringBootApplication
@ConfigurationPropertiesScan
public class JobradarApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobradarApplication.class, args);
	}

}
