package com.supermarket.supermarket_system.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("users_route", r -> r.path("/users/**")
                        .uri("lb://Users"))
                .route("items_route", r -> r.path("/items/**")
                        .uri("lb://Items"))
                .route("cart_route", r -> r.path("/cart/**")
                        .uri("lb://Cart"))
                .route("orders_route", r -> r.path("/orders/**")
                        .uri("lb://Orders"))
                .route("payment_route", r -> r.path("/payment/**")
                        .uri("lb://Payment"))
                .build();
    }
}