package com.supermarket.supermarket_system.dto.items;

public class ItemAvailabilityRequest {
    private Long itemId;
    private int requestedQuantity;

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public int getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(int requestedQuantity) { this.requestedQuantity = requestedQuantity; }
}

