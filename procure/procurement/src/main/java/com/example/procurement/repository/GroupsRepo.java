package com.example.procurement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.procurement.entity.Group;

@Repository
public interface GroupsRepo extends JpaRepository<Group, Long> {

  List<Group> findByName(String string);

  Optional<Group> findFirstByName(String string);

  boolean existsByCode(String code);

}
