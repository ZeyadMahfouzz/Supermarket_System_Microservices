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

    @Value("${app.rabbitmq.item-queue:items.queue}")
    private String itemQueue;

    @Value("${app.rabbitmq.item-exchange:items.exchange}")
    private String itemExchange;

    @Value("${app.rabbitmq.item-routing-key:items.routingkey}")
    private String itemRoutingKey;

    @Bean
    public Queue itemQueue() {
        return new Queue(itemQueue, true);
    }

    @Bean
    public DirectExchange itemExchange() {
        return new DirectExchange(itemExchange);
    }

    @Bean
    public Binding itemBinding() {
        return BindingBuilder.bind(itemQueue()).to(itemExchange()).with(itemRoutingKey);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jacksonMessageConverter());
        return template;
    }
}
