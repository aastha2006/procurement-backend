package com.example.procurement.repository;

import com.example.procurement.entity.GoodsReceipt;
import com.example.procurement.entity.SocietyUser;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SocietyUserRepository extends JpaRepository<SocietyUser, Long> {

	Optional<SocietyUser> findByEmail(String email);}
