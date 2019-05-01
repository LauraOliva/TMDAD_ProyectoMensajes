package com.tmdad.censura;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.tmdad.censura.model")
public class CensuraApplication {

	public static void main(String[] args) {
		SpringApplication.run(CensuraApplication.class, args);
	}

}
