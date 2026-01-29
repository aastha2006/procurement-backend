package com.example.procurement.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApiResponse 
{

    private boolean success;
    private String message;

 // Constructors
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}

