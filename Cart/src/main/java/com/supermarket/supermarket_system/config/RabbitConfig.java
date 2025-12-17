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

    @Value("${app.rabbitmq.cart-queue:cart.queue}")
    private String cartQueue;

    @Value("${app.rabbitmq.cart-exchange:cart.exchange}")
    private String cartExchange;

    @Value("${app.rabbitmq.cart-routing-key:cart.routingkey}")
    private String cartRoutingKey;

    @Bean
    public Queue cartQueue() {
        return new Queue(cartQueue, true);
    }

    @Bean
    public DirectExchange cartExchange() {
        return new DirectExchange(cartExchange);
    }

    @Bean
    public Binding cartBinding() {
        return BindingBuilder.bind(cartQueue()).to(cartExchange()).with(cartRoutingKey);
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
