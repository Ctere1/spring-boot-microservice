package com.techcareer.apigateway.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {

	@Value("${openapi.auth-url}")
	private String authUrl;

	@Value("${openapi.product-url}")
	private String productUrl;

	@Bean
	public OpenAPI myOpenAPI() {
		Server authServer = new Server();
		authServer.setUrl(authUrl);
		authServer.setDescription("Auth-Service");

		Server productServer = new Server();
		productServer.setUrl(productUrl);
		productServer.setDescription("Product-Service");

		Contact contact = new Contact();
		contact.setEmail("cemiltan896@gmail.com");
		contact.setName("Cemil Tan");
		contact.setUrl("https://github.com/Ctere1/");

		License mitLicense = new License().name("MIT License").url("https://choosealicense.com/licenses/mit/");

		Info info = new Info().title("Techcareer Java Bootcamp Final Project API").version("1.0").contact(contact)
				.description("This API exposes endpoints to manage tutorials.")
				// .termsOfService("https://github.com/Ctere1/")
				.license(mitLicense);

		final String securitySchemeName = "bearerAuth";
		return new OpenAPI().addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
				.components(
						new Components().addSecuritySchemes(securitySchemeName,
								new SecurityScheme().name(securitySchemeName).type(SecurityScheme.Type.HTTP)
										.scheme("bearer").bearerFormat("JWT")))
				.info(info).servers(List.of(authServer, productServer));
	}
}
