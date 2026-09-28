package com.example.bloodlink.controller;

import com.example.bloodlink.model.BloodGroup;
import com.example.bloodlink.model.DonationRecord;
import com.example.bloodlink.model.Donor;
import com.example.bloodlink.repository.BloodGroupRepository;
import com.example.bloodlink.service.DonationService;
import com.example.bloodlink.service.DonorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DonorController {

    private final DonorService donorService;
    private final DonationService donationService;
    private final BloodGroupRepository bloodGroupRepository;

    @Autowired
    public DonorController(DonorService donorService,
                           DonationService donationService,
                           BloodGroupRepository bloodGroupRepository) {
        this.donorService = donorService;
        this.donationService = donationService;
        this.bloodGroupRepository = bloodGroupRepository;
    }

    /**
     * Show Donor Registration Form.
     */
    @GetMapping("/donors/register")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("donor")) {
            model.addAttribute("donor", new Donor());
        }
        model.addAttribute("bloodGroups", bloodGroupRepository.findAll());
        return "donors/register";
    }

    /**
     * Process Donor Registration.
     */
    @PostMapping("/donors/register")
    public String registerDonor(@Valid @ModelAttribute("donor") Donor donor,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("bloodGroups", bloodGroupRepository.findAll());
            return "donors/register";
        }

        // Check for duplicate donor records
        if (donorService.isDuplicateDonor(donor)) {
            bindingResult.rejectValue("phone", "duplicate.phone", 
                    "A donor with this phone number or identical details is already registered.");
            model.addAttribute("errorMessage", 
                    "Registration failed: A donor with this phone number or identical details already exists in the registry.");
            model.addAttribute("bloodGroups", bloodGroupRepository.findAll());
            return "donors/register";
        }

        try {
            donorService.registerDonor(donor);
            redirectAttributes.addFlashAttribute("successMessage", "Donor registered successfully!");
            return "redirect:/donors";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("bloodGroups", bloodGroupRepository.findAll());
            return "donors/register";
        }
    }

    /**
     * View All Donors.
     */
    @GetMapping("/donors")
    public String listDonors(Model model) {
        List<Donor> donors = donorService.getAllDonors();
        model.addAttribute("donors", donors);
        model.addAttribute("donorService", donorService);
        return "donors/list";
    }

    /**
     * View Donor Details & Donation History.
     */
    @GetMapping("/donors/{id}")
    public String viewDonorDetails(@PathVariable("id") Long id, Model model) {
        Donor donor = donorService.getDonorById(id);
        boolean isEligible = donorService.isDonorEligible(donor);
        LocalDate nextEligibleDate = donorService.getNextEligibleDate(donor);
        List<DonationRecord> history = donationService.getDonationHistory(id);

        model.addAttribute("donor", donor);
        model.addAttribute("isEligible", isEligible);
        model.addAttribute("availabilityStatus", donorService.getAvailabilityStatus(donor));
        model.addAttribute("nextEligibleDate", nextEligibleDate);
        model.addAttribute("history", history);
        return "donors/details";
    }

    /**
     * Search Donors Page & Results.
     */
    @GetMapping("/search")
    public String searchDonors(@RequestParam(value = "bloodGroup", required = false) String bloodGroup,
                               @RequestParam(value = "city", required = false) String city,
                               Model model) {
        List<BloodGroup> bloodGroups = bloodGroupRepository.findAll();
        model.addAttribute("bloodGroups", bloodGroups);
        model.addAttribute("selectedBloodGroup", bloodGroup != null ? bloodGroup.trim() : "");
        model.addAttribute("selectedCity", city != null ? city.trim() : "");

        boolean isSearchSubmitted = (bloodGroup != null || city != null);
        model.addAttribute("hasSearched", isSearchSubmitted);

        if (isSearchSubmitted) {
            // Service layer enforces cooldown filtering
            List<Donor> availableDonors = donorService.searchAvailableDonors(bloodGroup, city);
            model.addAttribute("results", availableDonors);
        }

        return "search/results";
    }
}
