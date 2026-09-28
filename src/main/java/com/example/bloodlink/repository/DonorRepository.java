package com.example.bloodlink.repository;

import com.example.bloodlink.model.BloodGroup;
import com.example.bloodlink.model.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonorRepository extends JpaRepository<Donor, Long> {

    List<Donor> findByBloodGroupAndCityIgnoreCase(BloodGroup bloodGroup, String city);

    List<Donor> findByBloodGroup(BloodGroup bloodGroup);

    List<Donor> findByCityIgnoreCase(String city);

    long countByBloodGroup(BloodGroup bloodGroup);

    boolean existsByPhone(String phone);

    boolean existsByNameIgnoreCaseAndPhoneAndCityIgnoreCaseAndBloodGroup(String name, String phone, String city, BloodGroup bloodGroup);
}
