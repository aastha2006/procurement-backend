package com.example.procurement.repository;

import com.example.procurement.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<com.example.procurement.entity.Payment, Long> {}
