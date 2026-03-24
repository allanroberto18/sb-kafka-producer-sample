package br.com.alr.api.sbkafkaproducersample.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

  @Bean
  OpenAPI orderApi() {
    return new OpenAPI()
        .info(new Info()
            .title("Order API")
            .description("Spring Boot 4 sample using hexagonal architecture and the outbox pattern")
            .version("v1")
            .contact(new Contact().name("Sample API"))
            .license(new License().name("MIT")));
  }
}
