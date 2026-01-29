package com.example.procurement.DTO;
import lombok.Data;

@Data
public class LoginResponse 
{
    private String accesstoken;
    private String refreshtoken;
    private String message;
}
