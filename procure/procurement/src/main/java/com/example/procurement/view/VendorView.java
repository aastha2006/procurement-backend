package com.example.procurement.view;



import com.example.procurement.entity.Enum.SupplierStatus;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VendorView {
    private int id;
    private String name;
    private String emailId;
    private String code;
    private SupplierStatus status;
    private String address;
    private String country;
    private String state;
    private String city;
   

}
