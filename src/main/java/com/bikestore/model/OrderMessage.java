package com.bikestore.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Order Message Contract
 * Represents the message exchanged between services
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessage {
    
    private String pedidoId;
    
    private Double monto;
    
    private String clienteEmail;
    
    private PaymentStatus paymentStatus;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    private Integer retryCount = 0;

    public enum PaymentStatus {
        PENDING,
        PAID,
        FAILED
    }

    public OrderMessage(String pedidoId, Double monto, String clienteEmail) {
        this.pedidoId = pedidoId;
        this.monto = monto;
        this.clienteEmail = clienteEmail;
        this.paymentStatus = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.retryCount = 0;
    }
}
