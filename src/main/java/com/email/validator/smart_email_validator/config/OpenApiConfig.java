package com.email.validator.smart_email_validator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smartEmailValidatorOpenAPI() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title("Smart Email Validator API")
                                .version("1.0.0")
                                .description("""
                                        Smart Email Validator API.
                                        
                                        Provides email validation,
                                        domain validation,
                                        blacklist checking,
                                        DNS/MX validation,
                                        disposable email detection,
                                        configurable validation checks,
                                        scoring and subscription-based validation.
                                        """)
                                .contact(
                                        new Contact()
                                                .name("Smart Email Validator")
                                )
                );
    }
}