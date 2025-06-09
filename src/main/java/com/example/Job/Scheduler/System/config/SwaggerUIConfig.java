package com.example.Job.Scheduler.System.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SwaggerUIConfig {
    @Bean
    public OpenAPI jobSchedulerOpenAPI(){
        final String securitySchemeName = "bearerAuth";  // Changed from "JWT" to "bearerAuth"
        return new OpenAPI()
                .info(new Info()
                        .title("Job Scheduler System API")
                        .description("API for scheduling and managing background jobs")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@jobscheduler.com")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                ));
    }
    @Bean
    @Primary
    public SwaggerUiConfigProperties swaggerUiConfigProperties() {
        SwaggerUiConfigProperties properties = new SwaggerUiConfigProperties();
        properties.setUrl("/v3/api-docs");
        properties.setConfigUrl("/v3/api-docs/swagger-config");
        return properties;
    }
}
