package com.example.procurement.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.procurement.entity.PurchaseRequisition;

public interface PurchaseRequisitionRepository extends JpaRepository<PurchaseRequisition, Long> {
  Optional<PurchaseRequisition> findByPrNumber(String prNumber);

  @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "items")
  @Query("SELECT pr FROM PurchaseRequisition pr WHERE pr.id = :id")
  Optional<PurchaseRequisition> findByIdWithItems(Long id);

  @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "items")
  Page<PurchaseRequisition> findByRequestedBy(Long createdById, Pageable pageable);

  @Query("""
          SELECT pr FROM PurchaseRequisition pr
          ORDER BY pr.createdAt DESC
      """)
  List<PurchaseRequisition> findRecentPRs(Pageable pageable);

  @Query("""
          SELECT COUNT(pr)
          FROM PurchaseRequisition pr
          WHERE pr.status = 'APPROVED'
      """)
  long countPendingPRs();

  @Query("""
          SELECT COUNT(pr)
          FROM PurchaseRequisition pr
          WHERE pr.status = 'PENDING'
            AND pr.createdAt BETWEEN :startOfDay AND :endOfDay
      """)
  long countTodayPendingPRs(Instant startOfDay, Instant endOfDay);

  @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "items")
  @Query("""
          SELECT pr
          FROM PurchaseRequisition pr
          WHERE pr.status = 'APPROVED'
      """)
  Page<PurchaseRequisition> findApprovedPR(Pageable pageable);

  @Override
  @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "items")
  Page<PurchaseRequisition> findAll(Pageable pageable);
}
