package com.example.procurement.DTO;

import java.util.List;

import lombok.Data;

@Data
public class SubmitQuotationDto {
    private Long rfqId;
    private Long vendorId;
    private List<ItemPrice> items;
    private String deliveryTerms;
    private String warranty;
    private String paymentTerms;
   
}


