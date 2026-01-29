package com.example.procurement.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.procurement.entity.UserPasswordHistory;
public interface UserPasswordHistoryRepo extends JpaRepository<UserPasswordHistory,Integer>
{

  List<UserPasswordHistory> findTop5ByUserIdOrderByChangedAtDesc(Integer id);

   
 
}
