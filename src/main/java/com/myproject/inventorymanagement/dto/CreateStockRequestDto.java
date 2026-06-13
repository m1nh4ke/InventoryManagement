package com.myproject.inventorymanagement.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateStockRequestDto {
    private String type; // "IMPORT" or "EXPORT"
    private String reason;
    private List<ItemDto> items;

    @Data
    public static class ItemDto {
        private Long productId;
        private Integer quantity;
    }
}
