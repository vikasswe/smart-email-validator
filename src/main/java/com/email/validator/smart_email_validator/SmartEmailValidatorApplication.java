package com.email.validator.smart_email_validator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class SmartEmailValidatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartEmailValidatorApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {

		System.out.println();
		System.out.println("=================================================");
		System.out.println("        Smart Email Validator Started");
		System.out.println("=================================================");
		System.out.println("Server     : http://localhost:4563");
		System.out.println("Swagger UI : http://localhost:4563/swagger-ui.html");
		System.out.println("API Docs   : http://localhost:4563/v3/api-docs");
		System.out.println("=================================================");
		System.out.println();
	}

}
