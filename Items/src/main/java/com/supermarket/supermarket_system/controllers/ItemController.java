//// ========================
//// PACKAGE DECLARATION
//// ========================
//// This class belongs to the "controllers" package,
//// where we expose REST endpoints for the outside world.
//package com.supermarket.supermarket_system.controllers;
//import com.supermarket.supermarket_system.models.Item;
//
//// ========================
//// IMPORTS
//// ========================
//import com.supermarket.supermarket_system.repositories.ItemRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.http.ResponseEntity;
//import org.springframework.http.HttpStatus;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.List;
//
//// ========================
//// CONTROLLER CLASS
//// ========================
//// @RestController → Marks this class as a REST API controller.
//// It combines @Controller + @ResponseBody, so all methods return JSON (not HTML).
//@RestController
//@RequestMapping("/items")
//public class ItemController {
//
//    @Autowired
//    private ItemRepository itemRepository;
//
//    // Create a new item (ADMIN ONLY)
//    @PostMapping
//    public ResponseEntity<?> createItem(
//            @RequestHeader("X-User-Role") String role,
//            @RequestBody Item item) {
//
//        // Admin check
//        if (!"ADMIN".equalsIgnoreCase(role)) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(Map.of("error", "Access denied: Admins only"));
//        }
//
//        Item savedItem = itemRepository.save(item);
//        return ResponseEntity.ok(savedItem);
//    }
//
//
//    // Delete an item (ADMIN ONLY)
//    @DeleteMapping
//    public ResponseEntity<?> deleteItem(
//            @RequestHeader("X-User-Role") String role,
//            @RequestBody Map<String, Long> body) {
//
//        // Admin check
//        if (!"ADMIN".equalsIgnoreCase(role)) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(Map.of("error", "Access denied: Admins only"));
//        }
//
//        Long id = body.get("id");
//        if (id == null) {
//            return ResponseEntity.badRequest()
//                    .body(Map.of("error", "id is required"));
//        }
//
//        try {
//            itemRepository.deleteById(id);
//            return ResponseEntity.ok(Map.of("message", "Item deleted successfully"));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest()
//                    .body(Map.of("error", "Failed to delete item: " + e.getMessage()));
//        }
//    }
//
//    // Get all items (PUBLIC)
//    @GetMapping
//    public List<Item> getAllItems() {
//        return itemRepository.findAll();
//    }
//
//    // Get a single item by ID (PUBLIC)
////    @GetMapping("/details")
//    @GetMapping("/{id}")
//    public ResponseEntity<?> getItemsById(@RequestBody Map<String, Long> body) {
//        Long id = body.get("id");
//        if (id == null) {
//            return ResponseEntity.badRequest()
//                    .body(Map.of("error", "id is required"));
//        }
//
//        Item item = itemRepository.findById(id).orElse(null);
//        if (item == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("error", "Item not found"));
//        }
//
//        return ResponseEntity.ok(item);
//    }
//
//    // Modify existing item (ADMIN ONLY)
//    @PutMapping("/update")
//    public ResponseEntity<?> updateItem(
//            @RequestHeader("X-User-Role") String role,
//            @RequestBody Map<String, Object> body) {
//
//        // Admin check
//        if (!"ADMIN".equalsIgnoreCase(role)) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(Map.of("error", "Access denied: Admins only"));
//        }
//
//        Object idObj = body.get("id");
//        if (idObj == null) {
//            return ResponseEntity.badRequest()
//                    .body(Map.of("error", "id is required"));
//        }
//
//        Long id;
//        try {
//            id = idObj instanceof Number ?
//                    ((Number) idObj).longValue() :
//                    Long.parseLong(idObj.toString());
//        } catch (NumberFormatException e) {
//            return ResponseEntity.badRequest()
//                    .body(Map.of("error", "id must be a valid number"));
//        }
//
//        Item updatedItem = itemRepository.findById(id).map(item -> {
//            if (body.containsKey("name") && body.get("name") != null) {
//                item.setName((String) body.get("name"));
//            }
//            if (body.containsKey("price") && body.get("price") != null) {
//                Number priceNum = (Number) body.get("price");
//                item.setPrice(priceNum.doubleValue());
//            }
//            if (body.containsKey("quantity") && body.get("quantity") != null) {
//                Number quantityNum = (Number) body.get("quantity");
//                item.setQuantity(quantityNum.intValue());
//            }
//            if (body.containsKey("category") && body.get("category") != null) {
//                item.setCategory((String) body.get("category"));
//            }
//            if (body.containsKey("description") && body.get("description") != null) {
//                item.setDescription((String) body.get("description"));
//            }
//            return itemRepository.save(item);
//        }).orElse(null);
//
//        if (updatedItem == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("error", "Item not found"));
//        }
//
//        return ResponseEntity.ok(updatedItem);
//    }
//
//    @PostMapping("/deduct")
//    @Transactional
//    public ResponseEntity<Map<String, Object>> deductItemQuantity(@RequestBody Map<String, Object> request) {
//        System.out.println("=== HTTP DEDUCTION REQUEST RECEIVED ===");
//        System.out.println("Request: " + request);
//
//        Map<String, Object> response = new HashMap<>();
//
//        // Validate request
//        if (request == null || request.get("itemId") == null || request.get("quantity") == null) {
//            System.out.println("ERROR: Invalid request - missing itemId or quantity");
//            response.put("success", false);
//            response.put("message", "Invalid request: missing itemId or quantity");
//            return ResponseEntity.badRequest().body(response);
//        }
//
//        // Parse itemId
//        Number itemIdNum = (Number) request.get("itemId");
//        Long itemId = itemIdNum == null ? null : itemIdNum.longValue();
//        System.out.println("Parsed itemId: " + itemId);
//
//        // Parse quantity
//        Number quantityNum = (Number) request.get("quantity");
//        int quantity = quantityNum == null ? 0 : quantityNum.intValue();
//        System.out.println("Parsed quantity: " + quantity);
//
//        if (itemId == null || quantity <= 0) {
//            System.out.println("ERROR: Invalid itemId or quantity");
//            response.put("success", false);
//            response.put("message", "Invalid itemId or quantity");
//            return ResponseEntity.badRequest().body(response);
//        }
//
//        // Find item
//        Item item = itemRepository.findById(itemId).orElse(null);
//        System.out.println("Item found: " + (item != null ? item.getName() : "NULL"));
//
//        if (item == null) {
//            System.out.println("ERROR: Item not found with ID: " + itemId);
//            response.put("success", false);
//            response.put("message", "Item not found");
//            return ResponseEntity.badRequest().body(response);
//        }
//
//        // Check if enough quantity available
//        System.out.println("Item current quantity: " + item.getQuantity());
//        if (item.getQuantity() < quantity) {
//            System.out.println("ERROR: Not enough quantity. Requested: " + quantity + ", Available: " + item.getQuantity());
//            response.put("success", false);
//            response.put("message", "Not enough quantity available. Available: " + item.getQuantity());
//            return ResponseEntity.badRequest().body(response);
//        }
//
//        // Deduct quantity
//        int newQuantity = item.getQuantity() - quantity;
//        item.setQuantity(newQuantity);
//        itemRepository.save(item);
//        System.out.println("SUCCESS: Quantity deducted. New quantity: " + newQuantity);
//
//        // Return success response
//        response.put("success", true);
//        response.put("message", "Quantity deducted successfully");
//        response.put("remainingQuantity", newQuantity);
//
//        System.out.println("Response: " + response);
//        System.out.println("=== END HTTP DEDUCTION REQUEST ===");
//
//        return ResponseEntity.ok(response);
//    }
//
//    @PostMapping("/restore")
//    @Transactional
//    public ResponseEntity<Map<String, Object>> restoreItemQuantity(@RequestBody Map<String, Object> request) {
//        System.out.println("=== HTTP RESTORE REQUEST RECEIVED ===");
//        System.out.println("Request: " + request);
//
//        Map<String, Object> response = new HashMap<>();
//
//        // Validate request
//        if (request == null || request.get("itemId") == null || request.get("quantity") == null) {
//            System.out.println("ERROR: Invalid request - missing itemId or quantity");
//            response.put("success", false);
//            response.put("message", "Invalid request: missing itemId or quantity");
//            return ResponseEntity.badRequest().body(response);
//        }
//
//        // Parse itemId
//        Number itemIdNum = (Number) request.get("itemId");
//        Long itemId = itemIdNum == null ? null : itemIdNum.longValue();
//        System.out.println("Parsed itemId: " + itemId);
//
//        // Parse quantity
//        Number quantityNum = (Number) request.get("quantity");
//        int quantity = quantityNum == null ? 0 : quantityNum.intValue();
//        System.out.println("Parsed quantity to restore: " + quantity);
//
//        if (itemId == null || quantity <= 0) {
//            System.out.println("ERROR: Invalid itemId or quantity");
//            response.put("success", false);
//            response.put("message", "Invalid itemId or quantity");
//            return ResponseEntity.badRequest().body(response);
//        }
//
//        // Find item
//        Item item = itemRepository.findById(itemId).orElse(null);
//        System.out.println("Item found: " + (item != null ? item.getName() : "NULL"));
//
//        if (item == null) {
//            System.out.println("ERROR: Item not found with ID: " + itemId);
//            response.put("success", false);
//            response.put("message", "Item not found");
//            return ResponseEntity.badRequest().body(response);
//        }
//
//        // Restore quantity (add it back)
//        int newQuantity = item.getQuantity() + quantity;
//        item.setQuantity(newQuantity);
//        itemRepository.save(item);
//        System.out.println("SUCCESS: Quantity restored. New quantity: " + newQuantity);
//
//        // Return success response
//        response.put("success", true);
//        response.put("message", "Quantity restored successfully");
//        response.put("newQuantity", newQuantity);
//
//        System.out.println("Response: " + response);
//        System.out.println("=== END HTTP RESTORE REQUEST ===");
//
//        return ResponseEntity.ok(response);
//    }
//
//}
//THE ABOVE WAS NOT WORKING DUE TO ZEYAD SHENANIGANS SO I COMMENTED IT OUT



// ========================
// PACKAGE DECLARATION
// ========================
// This class belongs to the "controllers" package,
// where we expose REST endpoints for the outside world.
package com.supermarket.supermarket_system.controllers;
import com.supermarket.supermarket_system.models.Item;

// ========================
// IMPORTS
// ========================
import com.supermarket.supermarket_system.repositories.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ========================
// CONTROLLER CLASS
// ========================
// @RestController → Marks this class as a REST API controller.
// It combines @Controller + @ResponseBody, so all methods return JSON (not HTML).
@RestController
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    // Create a new item
    @PostMapping
    public Item createItem(@RequestBody Item item) {
        return itemRepository.save(item);
    }


    // Delete an item
    @DeleteMapping("/{id}")
    public String deleteItem(@PathVariable Long id) {
        itemRepository.deleteById(id);
        return "Item deleted successfully!";
    }

    // Get all items
    @GetMapping
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    // Get a single item by ID
    @GetMapping("/{id}")
    public Item getItemsById(@PathVariable Long id) {
        return itemRepository.findById(id).orElse(null);
    }

    // Modify existing item
    @PutMapping("/{id}")
    public Item updateItem(@PathVariable Long id, @RequestBody Item updatedItem) {
        return itemRepository.findById(id).map(item -> {
            if (updatedItem.getName() != null) {
                item.setName(updatedItem.getName());
            }
            if (updatedItem.getPrice() != null) {
                item.setPrice(updatedItem.getPrice());
            }
            if (updatedItem.getQuantity() != 0) {
                item.setQuantity(updatedItem.getQuantity());
            }
            if (updatedItem.getCategory() != null) {
                item.setCategory(updatedItem.getCategory());
            }
            if (updatedItem.getDescription() != null) {
                item.setDescription(updatedItem.getDescription());
            }
            if (updatedItem.getImageUrl() != null) {
                item.setImageUrl(updatedItem.getImageUrl());
            }
            return itemRepository.save(item);
        }).orElse(null);// If not found, return null
    }
}