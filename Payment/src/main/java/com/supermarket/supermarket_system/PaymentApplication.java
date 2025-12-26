// ============================================================================
// PaymentApplication.java - Spring Boot Application Main Class
// ============================================================================
/**
 * Main application class for Payment Microservice
 *
 * ANNOTATIONS:
 * @SpringBootApplication: Combines three annotations:
 *   1. @Configuration: Allows defining Spring beans
 *   2. @EnableAutoConfiguration: Auto-configures Spring based on dependencies
 *   3. @ComponentScan: Scans for Spring components in this package and sub-packages
 *
 * @EnableDiscoveryClient:
 *   - Enables service registration with Eureka Server
 *   - Allows service discovery by other microservices
 *   - Payment service will be discoverable as "Payment" in Eureka
 *
 * MICROSERVICES ARCHITECTURE:
 * This service is part of a larger microservices ecosystem
 * It registers itself with Eureka for:
 * - Load balancing
 * - Service discovery
 * - Health monitoring
 * - Failover capabilities
 */
        package com.supermarket.supermarket_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PaymentApplication {

    /**
     * Application entry point
     *
     * STARTUP PROCESS:
     * 1. Loads application.properties configuration
     * 2. Initializes Spring Application Context
     * 3. Connects to MySQL database (creates if not exists)
     * 4. Registers with Eureka Server at localhost:8761
     * 5. Starts embedded Tomcat server on random port (server.port=0)
     * 6. Begins accepting HTTP requests
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }
}
