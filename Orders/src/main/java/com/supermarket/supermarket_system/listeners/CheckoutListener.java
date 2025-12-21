package com.supermarket.supermarket_system.listeners;

import com.supermarket.supermarket_system.dto.cart.CartCheckoutEvent;
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
    public void handleCheckoutEvent(CartCheckoutEvent event) {
        log.info("Received checkout event for user {} with {} items and total price {}",
                event.getUserId(), event.getItems().size(), event.getTotalPrice());
        try {
            // Create order directly from the event data (no REST call needed)
            orderService.createOrderFromCheckoutEvent(event);
            log.info("Order created successfully from checkout event for user {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to process checkout event for user {}", event.getUserId(), e);
            // Consider implementing retry logic or dead letter queue here
        }
    }
}