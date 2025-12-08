package com.supermarket.supermarket_system.filter;

import com.supermarket.supermarket_system.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtils jwtUtils;

    // Endpoints that are completely public (no auth needed)
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/users/register",
            "/users/login",
            "/users/health"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();
        HttpMethod method = request.getMethod();

        // Allow fully public endpoints
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        // Allow GET requests to /items and /items/** without authentication
        if (HttpMethod.GET.equals(method) && path.startsWith("/items")) {
            return chain.filter(exchange);
        }

        // All other requests require authentication
        String authHeader = request.getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        // Validate JWT token
        if (!jwtUtils.validateToken(token)) {
            return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
        }

        // Extract user information from token
        String email = jwtUtils.getEmail(token);
        String role = jwtUtils.getRole(token);

        // Check role-based authorization
        if (requiresAdminRole(path, method) && !"ADMIN".equals(role)) {
            return onError(exchange, "Access denied. Admin role required.", HttpStatus.FORBIDDEN);
        }

        // Add user info to headers for downstream services
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Email", email)
                .header("X-User-Role", role)
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private boolean requiresAdminRole(String path, HttpMethod method) {
        // Items endpoints - POST, PUT, DELETE require ADMIN
        if (path.startsWith("/items")) {
            return HttpMethod.POST.equals(method) ||
                    HttpMethod.PUT.equals(method) ||
                    HttpMethod.DELETE.equals(method);
        }

        // Orders endpoints that require ADMIN
        if (path.startsWith("/orders/all") ||
                path.startsWith("/orders/status/")) {
            return true;
        }

        // Update order status - ADMIN only
        if (path.matches("/orders/[^/]+/status") && HttpMethod.PATCH.equals(method)) {
            return true;
        }

        return false;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");

        String errorResponse = String.format("{\"error\": \"%s\", \"status\": %d}",
                message, status.value());

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(errorResponse.getBytes()))
        );
    }

    @Override
    public int getOrder() {
        return -100; // Execute this filter first
    }
}