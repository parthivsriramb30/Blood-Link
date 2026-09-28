package com.example.bloodlink.repository;

import com.example.bloodlink.model.BloodGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BloodGroupRepository extends JpaRepository<BloodGroup, Long> {
    Optional<BloodGroup> findByGroupName(String groupName);
    Optional<BloodGroup> findByGroupNameIgnoreCase(String groupName);
    boolean existsByGroupName(String groupName);
}
