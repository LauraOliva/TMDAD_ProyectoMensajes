package com.tmdad.ficheros;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.tmdad.ficheros.model")
public class FicherosApplication {

	public static void main(String[] args) {
		SpringApplication.run(FicherosApplication.class, args);
	}

}
