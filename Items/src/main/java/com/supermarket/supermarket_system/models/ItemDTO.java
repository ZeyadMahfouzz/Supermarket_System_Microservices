package com.supermarket.supermarket_system.models;

import lombok.Data;
@Data
public class ItemDTO {
    private String itemName;
    private String itemDescription;
    private double itemPrice;
    private int itemQuantity;

    public ItemDTO(Item item) {
        this.itemName = item.getName();
        this.itemDescription = item.getDescription();
        this.itemPrice = item.getPrice();
        this.itemQuantity = item.getQuantity();
    }
}
