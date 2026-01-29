package com.example.procurement.DTO;

import lombok.Data;

@Data
public class RfqItemDto {
    private Long itemId;
    private String itemDescription;
    private Integer quantity;
    private String unit;
}

