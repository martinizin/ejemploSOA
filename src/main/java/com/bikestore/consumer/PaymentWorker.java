package com.bikestore.consumer;

import com.bikestore.config.RabbitConfig;
import com.bikestore.model.OrderMessage;
import com.bikestore.util.LoggingUtil;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Payment Worker
 * Processes payments with 50% failure probability
 * Retries 3 times before sending to DLQ
 */
@Service
public class PaymentWorker {

    private final RabbitTemplate rabbitTemplate;
    private final Random random = new Random();
    private static final int MAX_RETRIES = 3;

    public PaymentWorker(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitConfig.PAYMENTS_QUEUE)
    public void processPayment(OrderMessage order, Message message) {
        
        String pedidoId = order.getPedidoId();
        Integer retryCount = order.getRetryCount();
        
        LoggingUtil.logStep("PAYMENT_PROCESSING", pedidoId, 
            "Attempt " + (retryCount + 1) + "/" + MAX_RETRIES + " - Amount: $" + order.getMonto());

        // Simulate payment with 50% failure rate
        boolean paymentSuccess = random.nextBoolean();
        
        if (paymentSuccess) {
            // Payment successful
            order.setPaymentStatus(OrderMessage.PaymentStatus.PAID);
            LoggingUtil.logStep("PAYMENT_SUCCESS", pedidoId, 
                "Payment processed successfully - Status: PAID");
            
            // Send to email queue
            rabbitTemplate.convertAndSend(
                RabbitConfig.ORDERS_EXCHANGE,
                RabbitConfig.EMAILS_ROUTING_KEY,
                order
            );
            
            LoggingUtil.logStep("PAYMENT_FORWARDED_TO_EMAIL", pedidoId, 
                "Order sent to email queue");
            
        } else {
            // Payment failed
            order.setRetryCount(retryCount + 1);
            
            if (order.getRetryCount() >= MAX_RETRIES) {
                // Max retries reached - send to DLQ
                order.setPaymentStatus(OrderMessage.PaymentStatus.FAILED);
                
                LoggingUtil.logStep("PAYMENT_FAILED_MAX_RETRIES", pedidoId, 
                    "Payment failed after " + MAX_RETRIES + " attempts - Sending to DLQ");
                
                rabbitTemplate.convertAndSend(
                    RabbitConfig.ORDERS_EXCHANGE,
                    "orders.dead",
                    order
                );
                
            } else {
                // Retry
                LoggingUtil.logStep("PAYMENT_FAILED_RETRY", pedidoId, 
                    "Payment failed - Retry " + order.getRetryCount() + "/" + MAX_RETRIES);
                
                // Re-queue for retry
                rabbitTemplate.convertAndSend(
                    RabbitConfig.ORDERS_EXCHANGE,
                    RabbitConfig.PAYMENTS_ROUTING_KEY,
                    order
                );
            }
        }
    }
}
