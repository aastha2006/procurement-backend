package com.example.procurement.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "group_rls")
public class Group extends BaseEntity
{
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;

   @Column(unique = true)
   private String name;
   
   @Column(name = "code", length = 10, nullable = false, unique = true)
    private String code;          // MC, SM, PT, AS

    @Column(name = "type", length = 20, nullable = false)
    private String type;        
    @Column(name = "description", length = 255)
    private String description;
   @ManyToMany(mappedBy = "groups")
    @JsonIgnore
    private Set<Role> roles = new HashSet<>();

   public Group orElseThrow(Object object) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'orElseThrow'");
   }
}
