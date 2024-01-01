package com.techcareer.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity serverHttpSecurity) {
		serverHttpSecurity.csrf(csrf -> csrf.disable()
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.authorizeExchange(
						exchange -> exchange.pathMatchers("/eureka/**").permitAll()
								.pathMatchers("/swagger-ui/**").permitAll()
								.pathMatchers("/v3/api-docs/**").permitAll()
								.pathMatchers("/api/user/signin").permitAll()
								.pathMatchers("/api/user/signup").permitAll()
								.anyExchange().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())));
		return serverHttpSecurity.build();
	}

	private CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.applyPermitDefaultValues();

		configuration.addAllowedMethod(HttpMethod.GET);
		configuration.addAllowedMethod(HttpMethod.POST);
		configuration.addAllowedMethod(HttpMethod.DELETE);
		configuration.addAllowedMethod(HttpMethod.PUT);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}
}