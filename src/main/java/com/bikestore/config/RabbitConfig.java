package com.bikestore.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration
 * Defines exchanges, queues, bindings, DLQ, and retry logic
 */
@Configuration
public class RabbitConfig {

    // Exchange
    public static final String ORDERS_EXCHANGE = "orders.exchange";
    
    // Queues
    public static final String ORDERS_QUEUE = "orders.queue";
    public static final String PAYMENTS_QUEUE = "payments.queue";
    public static final String EMAILS_QUEUE = "emails.queue";
    public static final String ORDERS_DLQ = "orders.dlq";
    
    // Routing Keys
    public static final String ORDERS_ROUTING_KEY = "orders.created";
    public static final String PAYMENTS_ROUTING_KEY = "payments.process";
    public static final String EMAILS_ROUTING_KEY = "emails.send";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }

    // === Exchange ===
    @Bean
    public DirectExchange ordersExchange() {
        return new DirectExchange(ORDERS_EXCHANGE, true, false);
    }

    // === Main Queues ===
    @Bean
    public Queue ordersQueue() {
        return QueueBuilder.durable(ORDERS_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDERS_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "orders.dead")
                .build();
    }

    @Bean
    public Queue paymentsQueue() {
        return QueueBuilder.durable(PAYMENTS_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDERS_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "orders.dead")
                .build();
    }

    @Bean
    public Queue emailsQueue() {
        return QueueBuilder.durable(EMAILS_QUEUE).build();
    }

    // === Dead Letter Queue (DLQ) ===
    @Bean
    public Queue ordersDlq() {
        return QueueBuilder.durable(ORDERS_DLQ).build();
    }

    // === Bindings ===
    @Bean
    public Binding ordersBinding(Queue ordersQueue, DirectExchange ordersExchange) {
        return BindingBuilder.bind(ordersQueue)
                .to(ordersExchange)
                .with(ORDERS_ROUTING_KEY);
    }

    @Bean
    public Binding paymentsBinding(Queue paymentsQueue, DirectExchange ordersExchange) {
        return BindingBuilder.bind(paymentsQueue)
                .to(ordersExchange)
                .with(PAYMENTS_ROUTING_KEY);
    }

    @Bean
    public Binding emailsBinding(Queue emailsQueue, DirectExchange ordersExchange) {
        return BindingBuilder.bind(emailsQueue)
                .to(ordersExchange)
                .with(EMAILS_ROUTING_KEY);
    }

    @Bean
    public Binding dlqBinding(Queue ordersDlq, DirectExchange ordersExchange) {
        return BindingBuilder.bind(ordersDlq)
                .to(ordersExchange)
                .with("orders.dead");
    }
}
