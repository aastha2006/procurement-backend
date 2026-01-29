package com.example.procurement.DTO;

import java.time.LocalDate;

import lombok.Getter;

@Getter
public class UpdateUserLoginDTO {
  
private String typePassword;
 private  Boolean enabled;
  private LocalDate dt_disable;
    private LocalDate dt_enable;
  private Integer idgroup;

}
