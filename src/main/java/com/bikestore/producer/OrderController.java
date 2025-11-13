package com.bikestore.producer;

import com.bikestore.model.OrderMessage;
import com.bikestore.util.LoggingUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Order REST Controller
 * Exposes /orders endpoint to receive new orders
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderProducer orderProducer;

    public OrderController(OrderProducer orderProducer) {
        this.orderProducer = orderProducer;
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderMessage order) {
        
        // Generate pedidoId if not provided
        if (order.getPedidoId() == null || order.getPedidoId().isEmpty()) {
            order.setPedidoId(UUID.randomUUID().toString());
        }
        
        LoggingUtil.logStep("ORDER_RECEIVED", order.getPedidoId(), 
            "Received order for " + order.getClienteEmail() + " - Amount: $" + order.getMonto());
        
        orderProducer.publishOrder(order);
        
        return ResponseEntity.accepted()
                .body("Order accepted: " + order.getPedidoId());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("BikeStore Async v1.0 - UP");
    }
}
