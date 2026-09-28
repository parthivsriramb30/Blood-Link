package com.example.bloodlink.controller;

import com.example.bloodlink.exception.CooldownViolationException;
import com.example.bloodlink.model.Donor;
import com.example.bloodlink.service.DonationService;
import com.example.bloodlink.service.DonorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DonationController {

    private final DonationService donationService;
    private final DonorService donorService;

    @Autowired
    public DonationController(DonationService donationService, DonorService donorService) {
        this.donationService = donationService;
        this.donorService = donorService;
    }

    /**
     * Show Record Donation Form.
     * Optionally pre-select donor if donorId parameter is present.
     */
    @GetMapping("/donations/record")
    public String showRecordDonationForm(@RequestParam(value = "donorId", required = false) Long donorId,
                                         Model model) {
        List<Donor> donors = donorService.getAllDonors();
        model.addAttribute("donors", donors);
        model.addAttribute("donorService", donorService);
        model.addAttribute("selectedDonorId", donorId);
        model.addAttribute("defaultDate", LocalDate.now());
        return "donations/record";
    }

    /**
     * Handle submission of a new donation record.
     * Enforces cooldown verification through DonationService.
     */
    @PostMapping("/donations/record")
    public String processRecordDonation(
            @RequestParam("donorId") Long donorId,
            @RequestParam("donationDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate donationDate,
            @RequestParam("hospitalName") String hospitalName,
            @RequestParam(value = "notes", required = false) String notes,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (donorId == null) {
            model.addAttribute("errorMessage", "Please select a donor.");
            model.addAttribute("donors", donorService.getAllDonors());
            model.addAttribute("donorService", donorService);
            model.addAttribute("defaultDate", donationDate != null ? donationDate : LocalDate.now());
            return "donations/record";
        }

        if (hospitalName == null || hospitalName.trim().isEmpty()) {
            model.addAttribute("errorMessage", "Hospital name cannot be empty.");
            model.addAttribute("donors", donorService.getAllDonors());
            model.addAttribute("donorService", donorService);
            model.addAttribute("selectedDonorId", donorId);
            model.addAttribute("defaultDate", donationDate != null ? donationDate : LocalDate.now());
            return "donations/record";
        }

        try {
            donationService.recordDonation(donorId, donationDate, hospitalName.trim(), notes != null ? notes.trim() : "");
            redirectAttributes.addFlashAttribute("successMessage",
                    "Donation recorded successfully! Donor is now temporarily in the 90-day cooldown period.");
            return "redirect:/donors/" + donorId;
        } catch (CooldownViolationException ex) {
            model.addAttribute("errorMessage", ex.getMessage() + " Next eligible date: " + ex.getFormattedEligibleDate());
            model.addAttribute("donors", donorService.getAllDonors());
            model.addAttribute("donorService", donorService);
            model.addAttribute("selectedDonorId", donorId);
            model.addAttribute("defaultDate", donationDate);
            return "donations/record";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("donors", donorService.getAllDonors());
            model.addAttribute("donorService", donorService);
            model.addAttribute("selectedDonorId", donorId);
            model.addAttribute("defaultDate", donationDate);
            return "donations/record";
        }
    }
}
