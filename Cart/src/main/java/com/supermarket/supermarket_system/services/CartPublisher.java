package com.supermarket.supermarket_system.services;

import com.supermarket.supermarket_system.dto.payment.CartCheckoutEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CartPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.order-exchange:orders.exchange}")
    private String orderExchange;

    @Value("${app.rabbitmq.order-routing-key:orders.routingkey}")
    private String orderRoutingKey;

    public void publishCheckout(CartCheckoutEvent event) {
        log.info("Publishing checkout event for user {} to exchange {} with routing key {}",
                event.getUserId(), orderExchange, orderRoutingKey);

        try {
            rabbitTemplate.convertAndSend(orderExchange, orderRoutingKey, event);
            log.info("Successfully published checkout event for user {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to publish checkout event for user {}", event.getUserId(), e);
            throw new RuntimeException("Failed to publish checkout event", e);
        }
    }
}