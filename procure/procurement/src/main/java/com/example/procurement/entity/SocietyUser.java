package com.example.procurement.entity;

import java.time.LocalDate;

import com.example.procurement.entity.Enum.MemberRole;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class SocietyUser {
    @Id @GeneratedValue private Long id;
  
    private String fullName;
    private String email;
    private String flatNumber;
    private String block;
    private boolean owner;
  private String phone;
    @Enumerated(EnumType.STRING)
    private MemberRole role; // PRESIDENT, SECRETARY, TREASURER, COMMITTEE_MEMBER, RESIDENT

    private boolean committeeMember;
     private boolean active;
    private LocalDate joinedOn;
    
    @ManyToOne private Society society;
}
 

