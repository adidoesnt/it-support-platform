package com.adityabanerjee.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;

@SpringBootApplication
@EnableJdbcAuditing
public class ApiApplication {

	public static void main(String[] args) {
		System.out.println("Starting API application");
		SpringApplication.run(ApiApplication.class, args);
	}

}
