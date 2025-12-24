package com.supermarket.supermarket_system.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supermarket.supermarket_system.dto.cart.ItemDetailsDto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.HashMap;
import java.util.Map;

@Converter
public class ItemsMapConverter implements AttributeConverter<Map<String, ItemDetailsDto>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, ItemDetailsDto> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert itemDetails map to JSON", e);
        }
    }

    @Override
    public Map<String, ItemDetailsDto> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<Map<String, ItemDetailsDto>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }
}