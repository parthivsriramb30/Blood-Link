package com.example.bloodlink.service;

import com.example.bloodlink.exception.CooldownViolationException;
import com.example.bloodlink.exception.ResourceNotFoundException;
import com.example.bloodlink.model.BloodGroup;
import com.example.bloodlink.model.DonationRecord;
import com.example.bloodlink.model.Donor;
import com.example.bloodlink.repository.BloodGroupRepository;
import com.example.bloodlink.repository.DonationRecordRepository;
import com.example.bloodlink.repository.DonorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonorServiceTest {

    @Mock
    private DonorRepository donorRepository;

    @Mock
    private BloodGroupRepository bloodGroupRepository;

    @Mock
    private DonationRecordRepository donationRecordRepository;

    private DonorService donorService;
    private DonationService donationService;

    private BloodGroup bgOPlus;

    @BeforeEach
    void setUp() {
        donorService = new DonorService(donorRepository, bloodGroupRepository, donationRecordRepository);
        donationService = new DonationService(donationRecordRepository, donorRepository, donorService);
        bgOPlus = new BloodGroup(1L, "O+");
    }

    @Test
    @DisplayName("Case 4: Donor with no previous donation (lastDonationDate == null) is AVAILABLE")
    void testDonorWithNoPreviousDonationIsEligible() {
        Donor donor = new Donor("Arun Kumar", 24, "Male", "9876543210", "arun@example.com", "Coimbatore", bgOPlus);
        donor.setLastDonationDate(null);

        assertTrue(donorService.isDonorEligible(donor));
        assertEquals("AVAILABLE", donorService.getAvailabilityStatus(donor));
    }

    @Test
    @DisplayName("Case 5: Donor whose last donation was more than 90 days ago is AVAILABLE")
    void testDonorDonatedMoreThan90DaysAgoIsEligible() {
        Donor donor = new Donor("Priya S", 22, "Female", "9876543211", "priya@example.com", "Chennai", bgOPlus);
        donor.setLastDonationDate(LocalDate.now().minusDays(100)); // 100 days ago

        assertTrue(donorService.isDonorEligible(donor));
        assertEquals("AVAILABLE", donorService.getAvailabilityStatus(donor));
    }

    @Test
    @DisplayName("Case 6: Donor whose last donation was less than 90 days ago is in COOLDOWN")
    void testDonorDonatedLessThan90DaysAgoIsCooldown() {
        Donor donor = new Donor("Karthik R", 28, "Male", "9876543212", "karthik@example.com", "Erode", bgOPlus);
        LocalDate lastDonation = LocalDate.now().minusDays(30); // 30 days ago
        donor.setLastDonationDate(lastDonation);

        assertFalse(donorService.isDonorEligible(donor));
        assertEquals("COOLDOWN", donorService.getAvailabilityStatus(donor));
        assertEquals(lastDonation.plusDays(90), donorService.getNextEligibleDate(donor));
    }

    @Test
    @DisplayName("Case 8: Donor becoming eligible exactly after 90 days")
    void testDonorEligibleExactlyAfter90Days() {
        Donor donor = new Donor("Test Donor", 30, "Male", "9876543219", "test@example.com", "Salem", bgOPlus);
        donor.setLastDonationDate(LocalDate.now().minusDays(90)); // Exactly 90 days ago

        assertTrue(donorService.isDonorEligible(donor));
        assertEquals("AVAILABLE", donorService.getAvailabilityStatus(donor));
    }

    @Test
    @DisplayName("Case 2: Search returns ONLY eligible donors; excludes donors in 90-day cooldown")
    void testSearchExcludesCooldownDonors() {
        Donor eligibleDonor = new Donor("Arun Kumar", 24, "Male", "9876543210", "arun@example.com", "Coimbatore", bgOPlus);
        eligibleDonor.setLastDonationDate(null); // Eligible

        Donor cooldownDonor = new Donor("Meena K", 26, "Female", "9876543213", "meena@example.com", "Coimbatore", bgOPlus);
        cooldownDonor.setLastDonationDate(LocalDate.now().minusDays(40)); // In cooldown (40 days ago)

        when(bloodGroupRepository.findByGroupNameIgnoreCase("O+")).thenReturn(Optional.of(bgOPlus));
        when(donorRepository.findByBloodGroupAndCityIgnoreCase(bgOPlus, "Coimbatore"))
                .thenReturn(Arrays.asList(eligibleDonor, cooldownDonor));

        List<Donor> results = donorService.searchAvailableDonors("O+", "Coimbatore");

        assertEquals(1, results.size());
        assertEquals("Arun Kumar", results.get(0).getName());
        assertFalse(results.contains(cooldownDonor));
    }

    @Test
    @DisplayName("Case 3: Search when no donors exist returns empty list")
    void testSearchWhenNoDonorsExist() {
        when(bloodGroupRepository.findByGroupNameIgnoreCase("O+")).thenReturn(Optional.of(bgOPlus));
        when(donorRepository.findByBloodGroupAndCityIgnoreCase(bgOPlus, "NonExistentCity"))
                .thenReturn(List.of());

        List<Donor> results = donorService.searchAvailableDonors("O+", "NonExistentCity");
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Case 7: Attempt to record donation during 90-day cooldown throws CooldownViolationException")
    void testRecordDonationDuringCooldownThrowsException() {
        Donor donor = new Donor("Karthik R", 28, "Male", "9876543212", "karthik@example.com", "Erode", bgOPlus);
        donor.setId(10L);
        donor.setLastDonationDate(LocalDate.now().minusDays(20)); // Donated 20 days ago

        when(donorRepository.findById(10L)).thenReturn(Optional.of(donor));

        CooldownViolationException ex = assertThrows(CooldownViolationException.class, () ->
                donationService.recordDonation(10L, LocalDate.now(), "City Hospital", "Emergency")
        );

        assertTrue(ex.getMessage().contains("90-day cooldown"));
        assertNotNull(ex.getNextEligibleDate());
        verify(donationRecordRepository, never()).save(any(DonationRecord.class));
    }

    @Test
    @DisplayName("Case 1: Successful donation updates lastDonationDate and saves record")
    void testSuccessfulDonationUpdatesDonor() {
        Donor donor = new Donor("Arun Kumar", 24, "Male", "9876543210", "arun@example.com", "Coimbatore", bgOPlus);
        donor.setId(5L);
        donor.setLastDonationDate(null); // Eligible

        when(donorRepository.findById(5L)).thenReturn(Optional.of(donor));
        when(donationRecordRepository.save(any(DonationRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDate donationDate = LocalDate.now();
        donationService.recordDonation(5L, donationDate, "Apollo Hospital", "Voluntary");

        assertEquals(donationDate, donor.getLastDonationDate());
        verify(donorRepository, times(1)).save(donor);
        verify(donationRecordRepository, times(1)).save(any(DonationRecord.class));
    }

    @Test
    @DisplayName("Case 10: Unknown donor ID throws ResourceNotFoundException")
    void testUnknownDonorIdThrowsException() {
        when(donorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                donorService.getDonorById(999L)
        );
    }

    @Test
    @DisplayName("Duplicate Prevention: Registering duplicate donor throws IllegalArgumentException")
    void testRegisterDuplicateDonorThrowsException() {
        Donor donor = new Donor("Arun Kumar", 24, "Male", "9876543210", "arun@example.com", "Coimbatore", bgOPlus);
        when(bloodGroupRepository.findById(1L)).thenReturn(Optional.of(bgOPlus));
        when(donorRepository.existsByPhone("9876543210")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                donorService.registerDonor(donor)
        );

        assertTrue(ex.getMessage().contains("already registered"));
        verify(donorRepository, never()).save(any(Donor.class));
    }
}
