package com.bikestore.producer;

import com.bikestore.config.RabbitConfig;
import com.bikestore.model.OrderMessage;
import com.bikestore.util.LoggingUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Order Producer
 * Publishes order messages to RabbitMQ
 */
@Service
public class OrderProducer {

    private final RabbitTemplate rabbitTemplate;

    public OrderProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrder(OrderMessage order) {
        
        LoggingUtil.logStep("ORDER_PUBLISHED", order.getPedidoId(), 
            "Publishing to exchange: " + RabbitConfig.ORDERS_EXCHANGE);
        
        // Publish to payments queue for processing
        rabbitTemplate.convertAndSend(
            RabbitConfig.ORDERS_EXCHANGE,
            RabbitConfig.PAYMENTS_ROUTING_KEY,
            order
        );
        
        LoggingUtil.logStep("ORDER_SENT_TO_PAYMENT", order.getPedidoId(), 
            "Message sent to payment processing");
    }
}
