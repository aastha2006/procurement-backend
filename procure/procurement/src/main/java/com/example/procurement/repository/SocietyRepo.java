package com.example.procurement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.procurement.entity.Society;



@Repository
public interface SocietyRepo extends JpaRepository<Society,Integer>
{
      
}
