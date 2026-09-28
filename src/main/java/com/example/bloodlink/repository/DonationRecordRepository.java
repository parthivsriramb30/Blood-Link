package com.example.bloodlink.repository;

import com.example.bloodlink.model.DonationRecord;
import com.example.bloodlink.model.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonationRecordRepository extends JpaRepository<DonationRecord, Long> {

    List<DonationRecord> findByDonorOrderByDonationDateDesc(Donor donor);

    List<DonationRecord> findByDonorIdOrderByDonationDateDesc(Long donorId);
}
