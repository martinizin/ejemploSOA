package com.bikestore.consumer;

import com.bikestore.config.RabbitConfig;
import com.bikestore.model.OrderMessage;
import com.bikestore.util.LoggingUtil;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Email Worker
 * Sends confirmation email ONLY if paymentStatus = PAID
 */
@Service
public class EmailWorker {

    @RabbitListener(queues = RabbitConfig.EMAILS_QUEUE)
    public void sendEmail(OrderMessage order) {
        
        String pedidoId = order.getPedidoId();
        
        LoggingUtil.logStep("EMAIL_RECEIVED", pedidoId, 
            "Email task received - Status: " + order.getPaymentStatus());

        // Only process if payment is PAID
        if (order.getPaymentStatus() == OrderMessage.PaymentStatus.PAID) {
            
            LoggingUtil.logStep("EMAIL_SENDING", pedidoId, 
                "Sending confirmation email to: " + order.getClienteEmail());
            
            // Simulate email sending
            try {
                Thread.sleep(500); // Simulate email service delay
                
                LoggingUtil.logStep("EMAIL_SENT", pedidoId, 
                    "✓ Confirmation email sent successfully to " + order.getClienteEmail());
                
            } catch (InterruptedException e) {
                LoggingUtil.logStep("EMAIL_ERROR", pedidoId, 
                    "✗ Error sending email: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
            
        } else {
            LoggingUtil.logStep("EMAIL_SKIPPED", pedidoId, 
                "Email NOT sent - Payment status is " + order.getPaymentStatus());
        }
    }
}
