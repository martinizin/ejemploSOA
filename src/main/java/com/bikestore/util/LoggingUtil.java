package com.bikestore.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logging Utility
 * Provides consistent logging with pedidoId, timestamp, and thread info
 */
public class LoggingUtil {

    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static void logStep(String step, String pedidoId, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String threadName = Thread.currentThread().getName();
        
        System.out.printf("[%s] [%s] [%s] PedidoId=%s | %s%n", 
            timestamp, 
            threadName, 
            step, 
            pedidoId, 
            message
        );
    }

    public static void logError(String step, String pedidoId, String error) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String threadName = Thread.currentThread().getName();
        
        System.err.printf("[%s] [%s] [ERROR-%s] PedidoId=%s | %s%n", 
            timestamp, 
            threadName, 
            step, 
            pedidoId, 
            error
        );
    }
}
