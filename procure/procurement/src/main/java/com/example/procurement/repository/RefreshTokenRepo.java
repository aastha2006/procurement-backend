package com.example.procurement.repository;

import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.procurement.entity.RefreshToken;





public interface RefreshTokenRepo extends JpaRepository<RefreshToken,Integer>{

  
  Optional<RefreshToken> findByToken(String refreshToken);

  RefreshToken findByUserId(Integer id);

 
  
}
