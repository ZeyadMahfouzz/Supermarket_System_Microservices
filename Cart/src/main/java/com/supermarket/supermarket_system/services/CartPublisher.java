package com.supermarket.supermarket_system.services;

import com.supermarket.supermarket_system.dto.cart.CartCheckoutEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CartPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public CartPublisher(RabbitTemplate rabbitTemplate,
                        @Value("${app.rabbitmq.cart-exchange:cart.exchange}") String exchange,
                        @Value("${app.rabbitmq.cart-routing-key:cart.routingkey}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publishCheckout(CartCheckoutEvent event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}

