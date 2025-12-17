package com.supermarket.supermarket_system.listeners;

import com.supermarket.supermarket_system.dto.CheckoutEventDto;
import com.supermarket.supermarket_system.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheckoutListener {

    private final OrderService orderService;

    @RabbitListener(queues = "${app.rabbitmq.order-queue:orders.queue}")
    public void handleCheckoutEvent(CheckoutEventDto event) {
        log.info("Received checkout event for user {}", event.getUserId());
        try {
            // Reuse existing Orders service checkout flow which fetches cart via REST
            orderService.createOrderFromCart(event.getUserId(), event.getPaymentMethod());
            log.info("Order created from checkout event for user {} via createOrderFromCart", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to process checkout event for user {}", event.getUserId(), e);
        }
    }
}
