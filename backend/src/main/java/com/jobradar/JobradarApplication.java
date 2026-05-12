package com.jobradar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class JobradarApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobradarApplication.class, args);
	}

}
