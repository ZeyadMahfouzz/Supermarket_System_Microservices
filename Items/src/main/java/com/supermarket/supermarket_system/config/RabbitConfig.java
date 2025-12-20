package com.supermarket.supermarket_system.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${app.rabbitmq.item-exchange:items.exchange}")
    private String itemExchange;

    @Value("${app.rabbitmq.item-queue:items.queue}")
    private String itemQueue;

    @Value("${app.rabbitmq.item-routing-key:items.routingkey}")
    private String itemRoutingKey;

    @Value("${app.rabbitmq.item-deduct-queue:items.deduct.queue}")
    private String itemDeductQueue;

    @Value("${app.rabbitmq.item-deduct-routing-key:items.deduct.routingkey}")
    private String itemDeductRoutingKey;

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonMessageConverter());
        return factory;
    }

    // Exchange - Changed to DirectExchange to match existing
    @Bean
    public DirectExchange itemsExchange() {
        return new DirectExchange(itemExchange, true, false);
    }

    // Queue for availability checks
    @Bean
    public Queue itemsQueue() {
        return new Queue(itemQueue, true);
    }

    // Queue for deduction requests
    @Bean
    public Queue itemsDeductQueue() {
        return new Queue(itemDeductQueue, true);
    }

    // Binding for availability checks
    @Bean
    public Binding itemsBinding(Queue itemsQueue, DirectExchange itemsExchange) {
        return BindingBuilder.bind(itemsQueue)
                .to(itemsExchange)
                .with(itemRoutingKey);
    }

    // Binding for deduction requests
    @Bean
    public Binding itemsDeductBinding(Queue itemsDeductQueue, DirectExchange itemsExchange) {
        return BindingBuilder.bind(itemsDeductQueue)
                .to(itemsExchange)
                .with(itemDeductRoutingKey);
    }
}