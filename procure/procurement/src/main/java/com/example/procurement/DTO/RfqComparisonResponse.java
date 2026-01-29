package com.example.procurement.DTO;

import java.util.List;

import lombok.Data;

@Data
public class RfqComparisonResponse {
    private Long rfqId;
    private String prNumber;

    private List<RfqItemDto> items;
    private List<RfqVendorComparisonDto> vendors;

    private String recommendation;
}

