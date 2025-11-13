package com.bikestore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BikeStore Async v1.0 - Main Application
 * POC for asynchronous messaging with RabbitMQ
 */
@SpringBootApplication
public class Application {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        System.out.println("\n=== BikeStore Async v1.0 Started ===");
        System.out.println("API: http://localhost:8080/orders");
        System.out.println("RabbitMQ Management: http://localhost:15672 (admin/admin123)\n");
    }
}
