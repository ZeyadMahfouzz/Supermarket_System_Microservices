package com.supermarket.supermarket_system.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitConfig {

    @Value("${app.rabbitmq.order-queue:orders.queue}")
    private String orderQueue;

    @Value("${app.rabbitmq.order-exchange:orders.exchange}")
    private String orderExchange;

    @Value("${app.rabbitmq.order-routing-key:orders.routingkey}")
    private String orderRoutingKey;

    // Declare the queue (durable = true means it survives broker restart)
    @Bean
    public Queue orderQueue() {
        return new Queue(orderQueue, true);
    }

    // Declare the exchange
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(orderExchange);
    }

    // Bind queue to exchange with routing key
    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue()).to(orderExchange()).with(orderRoutingKey);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jacksonMessageConverter());
        // make RPC calls fail fast if items service doesn't reply
        template.setReplyTimeout(5000);
        return template;
    }
}