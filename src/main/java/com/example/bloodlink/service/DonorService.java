package com.example.bloodlink.service;

import com.example.bloodlink.exception.ResourceNotFoundException;
import com.example.bloodlink.model.BloodGroup;
import com.example.bloodlink.model.Donor;
import com.example.bloodlink.repository.BloodGroupRepository;
import com.example.bloodlink.repository.DonationRecordRepository;
import com.example.bloodlink.repository.DonorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DonorService {

    public static final int COOLDOWN_DAYS = 90;

    private final DonorRepository donorRepository;
    private final BloodGroupRepository bloodGroupRepository;
    private final DonationRecordRepository donationRecordRepository;

    @Autowired
    public DonorService(DonorRepository donorRepository,
                        BloodGroupRepository bloodGroupRepository,
                        DonationRecordRepository donationRecordRepository) {
        this.donorRepository = donorRepository;
        this.bloodGroupRepository = bloodGroupRepository;
        this.donationRecordRepository = donationRecordRepository;
    }

    /**
     * Checks if a donor is eligible to donate.
     * Core business rule:
     * - If never donated (lastDonationDate == null) -> AVAILABLE
     * - If lastDonationDate + 90 days <= currentDate -> AVAILABLE
     * - Otherwise -> UNAVAILABLE (in COOLDOWN)
     */
    public boolean isDonorEligible(Donor donor) {
        if (donor == null || !donor.isActive()) {
            return false;
        }
        if (donor.getLastDonationDate() == null) {
            return true;
        }
        LocalDate eligibleDate = donor.getLastDonationDate().plusDays(COOLDOWN_DAYS);
        LocalDate today = LocalDate.now();
        return !today.isBefore(eligibleDate);
    }

    /**
     * Alias for isDonorEligible for requirement naming compatibility.
     */
    public boolean isEligible(Donor donor) {
        return isDonorEligible(donor);
    }

    /**
     * Returns the date when the donor will be eligible to donate again.
     */
    public LocalDate getNextEligibleDate(Donor donor) {
        if (donor == null || donor.getLastDonationDate() == null) {
            return null;
        }
        return donor.getLastDonationDate().plusDays(COOLDOWN_DAYS);
    }

    /**
     * Returns the status label ("AVAILABLE" or "COOLDOWN").
     */
    public String getAvailabilityStatus(Donor donor) {
        return isDonorEligible(donor) ? "AVAILABLE" : "COOLDOWN";
    }

    /**
     * Checks if a donor with identical details or phone number already exists in the registry.
     */
    @Transactional(readOnly = true)
    public boolean isDuplicateDonor(Donor donor) {
        if (donor == null) {
            return false;
        }

        // Check if another donor with the same phone number already exists
        if (donor.getPhone() != null && !donor.getPhone().trim().isEmpty()) {
            if (donorRepository.existsByPhone(donor.getPhone().trim())) {
                return true;
            }
        }

        // Check if an identical donor record with same name, phone, city, and blood group exists
        if (donor.getBloodGroup() != null && donor.getBloodGroup().getId() != null) {
            BloodGroup bg = bloodGroupRepository.findById(donor.getBloodGroup().getId()).orElse(null);
            if (bg != null && donor.getName() != null && donor.getCity() != null) {
                if (donorRepository.existsByNameIgnoreCaseAndPhoneAndCityIgnoreCaseAndBloodGroup(
                        donor.getName().trim(), donor.getPhone().trim(), donor.getCity().trim(), bg)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Registers a new donor. Rejects duplicate donor records.
     */
    public Donor registerDonor(Donor donor) {
        if (donor.getBloodGroup() != null && donor.getBloodGroup().getId() != null) {
            BloodGroup bg = bloodGroupRepository.findById(donor.getBloodGroup().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Selected Blood Group does not exist"));
            donor.setBloodGroup(bg);
        }

        // SERVICE LAYER VALIDATION: Prevent duplicate records
        if (isDuplicateDonor(donor)) {
            throw new IllegalArgumentException("A donor with this phone number or identical details is already registered.");
        }

        return donorRepository.save(donor);
    }

    /**
     * Retrieves all registered donors.
     */
    @Transactional(readOnly = true)
    public List<Donor> getAllDonors() {
        return donorRepository.findAll();
    }

    /**
     * Retrieves a donor by their unique ID.
     */
    @Transactional(readOnly = true)
    public Donor getDonorById(Long id) {
        return donorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found with ID: " + id));
    }

    /**
     * Searches for donors matching blood group and city, strictly filtering out
     * donors currently in the 90-day cooldown period.
     */
    @Transactional(readOnly = true)
    public List<Donor> searchAvailableDonors(String bloodGroupName, String city) {
        List<Donor> matchedDonors;

        boolean hasBloodGroup = bloodGroupName != null && !bloodGroupName.trim().isEmpty();
        boolean hasCity = city != null && !city.trim().isEmpty();

        if (hasBloodGroup && hasCity) {
            Optional<BloodGroup> bgOpt = bloodGroupRepository.findByGroupNameIgnoreCase(bloodGroupName.trim());
            if (bgOpt.isPresent()) {
                matchedDonors = donorRepository.findByBloodGroupAndCityIgnoreCase(bgOpt.get(), city.trim());
            } else {
                matchedDonors = Collections.emptyList();
            }
        } else if (hasBloodGroup) {
            Optional<BloodGroup> bgOpt = bloodGroupRepository.findByGroupNameIgnoreCase(bloodGroupName.trim());
            matchedDonors = bgOpt.map(donorRepository::findByBloodGroup).orElse(Collections.emptyList());
        } else if (hasCity) {
            matchedDonors = donorRepository.findByCityIgnoreCase(city.trim());
        } else {
            matchedDonors = donorRepository.findAll();
        }

        // STRICT SERVICE LAYER ENFORCEMENT: Filter only eligible donors
        return matchedDonors.stream()
                .filter(this::isDonorEligible)
                .collect(Collectors.toList());
    }

    /**
     * Counts total registered donors.
     */
    @Transactional(readOnly = true)
    public long getTotalDonorsCount() {
        return donorRepository.count();
    }

    /**
     * Counts currently available donors.
     */
    @Transactional(readOnly = true)
    public long getAvailableDonorsCount() {
        return donorRepository.findAll().stream()
                .filter(this::isDonorEligible)
                .count();
    }

    /**
     * Counts donors currently in cooldown.
     */
    @Transactional(readOnly = true)
    public long getCooldownDonorsCount() {
        return donorRepository.findAll().stream()
                .filter(donor -> !isDonorEligible(donor))
                .count();
    }

    /**
     * Calculates donor counts grouped by each blood group.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getDonorCountByBloodGroup() {
        List<BloodGroup> bloodGroups = bloodGroupRepository.findAll();
        Map<String, Long> counts = new LinkedHashMap<>();
        for (BloodGroup bg : bloodGroups) {
            counts.put(bg.getGroupName(), donorRepository.countByBloodGroup(bg));
        }
        return counts;
    }

    /**
     * Saves or updates a donor entity.
     */
    public Donor save(Donor donor) {
        return donorRepository.save(donor);
    }
}
