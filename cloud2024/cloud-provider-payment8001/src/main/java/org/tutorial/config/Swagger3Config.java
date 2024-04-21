package org.tutorial.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class Swagger3Config {

	@Bean
	public GroupedOpenApi payApi() {
		return GroupedOpenApi.builder().group("支付微服務模塊")
				.pathsToMatch("/pay/**").build();
	}

	@Bean
	public GroupedOpenApi otherApi() {
		return GroupedOpenApi.builder().group("其他微服務模塊")
				.pathsToMatch("/other/**").build();
	}

	@Bean
	public OpenAPI docsOpenApi() {
		return new OpenAPI().info(new Info().title("spring-cloud")
				.description("通用設計").version("v1.0"))
				.externalDocs(new ExternalDocumentation().description("").url(""));
	}
}
