package com.supermarket.supermarket_system.config;

import com.supermarket.supermarket_system.filter.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Autowired
    JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        // Public endpoints
                        .pathMatchers("/user/register", "/user/login", "/health").permitAll()

                        // Item endpoints
                        .pathMatchers(HttpMethod.GET, "/items", "/items/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/items").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/items/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/items/**").hasRole("ADMIN")

                        // Cart endpoints
                        .pathMatchers("/cart/**").authenticated()

                        // Order endpoints
                        .pathMatchers(HttpMethod.POST, "/order/*/checkout").authenticated()
                        .pathMatchers(HttpMethod.GET, "/order/*/details").authenticated()
                        .pathMatchers(HttpMethod.GET, "/order/user/**").authenticated()
                        .pathMatchers(HttpMethod.DELETE, "/order/*/cancel").authenticated()
                        .pathMatchers(HttpMethod.GET, "/order/all").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/order/status/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/order/*/status").hasRole("ADMIN")

                        // All other requests
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}