package com.supermarket.supermarket_system.listeners;

import com.supermarket.supermarket_system.models.Item;
import com.supermarket.supermarket_system.repositories.ItemRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Component
public class ItemDeductionListener {

    @Autowired
    private ItemRepository itemRepository;

    @RabbitListener(queues = "${app.rabbitmq.item-deduct-queue}")
    @Transactional
    public Map<String, Object> handleDeductRequest(Map<String, Object> req) {
        itemRepository.findAll().forEach(i ->
                System.out.println("DB ITEM: id=" + i.getId() + ", name=" + i.getName())
        );

        System.out.println("=== DEDUCTION REQUEST RECEIVED ===");
        System.out.println("Request: " + req);

        Map<String, Object> resp = new HashMap<>();

        // Validate request
        if (req == null || req.get("itemId") == null || req.get("quantity") == null) {
            System.out.println("ERROR: Invalid request - missing itemId or quantity");
            resp.put("success", false);
            resp.put("message", "Invalid request: missing itemId or quantity");
            return resp;
        }

        // Parse itemId
        Number itemIdNum = (Number) req.get("itemId");
        Long itemId = itemIdNum == null ? null : itemIdNum.longValue();
        System.out.println("Parsed itemId: " + itemId);

        // Parse quantity
        Number quantityNum = (Number) req.get("quantity");
        int quantity = quantityNum == null ? 0 : quantityNum.intValue();
        System.out.println("Parsed quantity: " + quantity);

        if (itemId == null || quantity <= 0) {
            System.out.println("ERROR: Invalid itemId or quantity");
            resp.put("success", false);
            resp.put("message", "Invalid itemId or quantity");
            return resp;
        }

        // Find item
        Item item = itemRepository.findById(itemId).orElse(null);
        System.out.println("Item found: " + (item != null ? item.getName() : "NULL"));

        if (item == null) {
            System.out.println("ERROR: Item not found with ID: " + itemId);
            resp.put("success", false);
            resp.put("message", "Item not found");
            return resp;
        }

        // Check if enough quantity available
        System.out.println("Item current quantity: " + item.getQuantity());
        if (item.getQuantity() < quantity) {
            System.out.println("ERROR: Not enough quantity. Requested: " + quantity + ", Available: " + item.getQuantity());
            resp.put("success", false);
            resp.put("message", "Not enough quantity available. Available: " + item.getQuantity());
            return resp;
        }

        // Deduct quantity
        int newQuantity = item.getQuantity() - quantity;
        item.setQuantity(newQuantity);
        itemRepository.save(item);
        System.out.println("SUCCESS: Quantity deducted. New quantity: " + newQuantity);

        // Return success response
        resp.put("success", true);
        resp.put("message", "Quantity deducted successfully");
        resp.put("remainingQuantity", newQuantity);

        System.out.println("Response: " + resp);
        System.out.println("=== END DEDUCTION REQUEST ===");
        return resp;
    }
}