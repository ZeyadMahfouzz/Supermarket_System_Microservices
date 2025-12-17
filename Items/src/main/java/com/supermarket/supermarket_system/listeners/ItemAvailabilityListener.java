package com.supermarket.supermarket_system.listeners;

import com.supermarket.supermarket_system.models.Item;
import com.supermarket.supermarket_system.repositories.ItemRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@Component
public class ItemAvailabilityListener {

    @Autowired
    private ItemRepository itemRepository;

    @RabbitListener(queues = "${app.rabbitmq.item-queue:items.queue}")
    public Map<String, Object> handleAvailabilityRequest(Map<String, Object> req) {
        Map<String, Object> resp = new HashMap<>();
        if (req == null || req.get("itemId") == null) {
            resp.put("available", false);
            resp.put("availableQuantity", 0);
            return resp;
        }

        Number itemIdNum = (Number) req.get("itemId");
        Long itemId = itemIdNum == null ? null : itemIdNum.longValue();
        if (itemId == null) {
            resp.put("available", false);
            resp.put("availableQuantity", 0);
            return resp;
        }

        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            resp.put("available", false);
            resp.put("availableQuantity", 0);
            return resp;
        }

        resp.put("available", item.getQuantity() > 0);
        resp.put("availableQuantity", item.getQuantity());
        resp.put("unitPrice", item.getPrice());
        return resp;
    }
}
