package com.example.bloodlink.service;

import com.example.bloodlink.exception.CooldownViolationException;
import com.example.bloodlink.exception.ResourceNotFoundException;
import com.example.bloodlink.model.DonationRecord;
import com.example.bloodlink.model.Donor;
import com.example.bloodlink.repository.DonationRecordRepository;
import com.example.bloodlink.repository.DonorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class DonationService {

    private final DonationRecordRepository donationRecordRepository;
    private final DonorRepository donorRepository;
    private final DonorService donorService;

    @Autowired
    public DonationService(DonationRecordRepository donationRecordRepository,
                           DonorRepository donorRepository,
                           DonorService donorService) {
        this.donationRecordRepository = donationRecordRepository;
        this.donorRepository = donorRepository;
        this.donorService = donorService;
    }

    /**
     * Records a new blood donation.
     * Enforces the 90-day cooldown check in the Service Layer.
     */
    public DonationRecord recordDonation(Long donorId, LocalDate donationDate, String hospitalName, String notes) {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found with ID: " + donorId));

        // SERVICE LAYER VALIDATION: Cooldown Enforcement
        if (!donorService.isDonorEligible(donor)) {
            LocalDate nextDate = donorService.getNextEligibleDate(donor);
            throw new CooldownViolationException(
                    "This donor is currently in the 90-day cooldown period and cannot donate again yet.",
                    nextDate
            );
        }

        if (donationDate == null) {
            donationDate = LocalDate.now();
        }

        if (donationDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Donation date cannot be in the future.");
        }

        DonationRecord record = new DonationRecord();
        record.setDonor(donor);
        record.setDonationDate(donationDate);
        record.setHospitalName(hospitalName);
        record.setNotes(notes);

        DonationRecord savedRecord = donationRecordRepository.save(record);

        // Update Donor's lastDonationDate and persist
        donor.setLastDonationDate(donationDate);
        donorRepository.save(donor);

        return savedRecord;
    }

    /**
     * Retrieves donation history for a specific donor ordered by date descending.
     */
    @Transactional(readOnly = true)
    public List<DonationRecord> getDonationHistory(Long donorId) {
        // Ensure donor exists
        if (!donorRepository.existsById(donorId)) {
            throw new ResourceNotFoundException("Donor not found with ID: " + donorId);
        }
        return donationRecordRepository.findByDonorIdOrderByDonationDateDesc(donorId);
    }

    /**
     * Returns total number of blood donations recorded.
     */
    @Transactional(readOnly = true)
    public long getTotalDonationsCount() {
        return donationRecordRepository.count();
    }
}
