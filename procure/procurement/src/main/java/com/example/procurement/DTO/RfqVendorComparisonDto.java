package com.example.procurement.DTO;

import lombok.Data;

@Data
public class RfqVendorComparisonDto {

    private Long vendorId;
    private String vendorName;
    private String vendorCode;
    private Double rating;

    private Double basePrice;
    private Double gst;
    private Double total;

    private String delivery;
    private String warranty;
    private String paymentTerms;

    private boolean isLowest;
    private boolean isBestValue;
}

