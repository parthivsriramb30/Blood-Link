package com.example.bloodlink.controller;

import com.example.bloodlink.service.DonationService;
import com.example.bloodlink.service.DonorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final DonorService donorService;
    private final DonationService donationService;

    @Autowired
    public HomeController(DonorService donorService,
                          DonationService donationService) {
        this.donorService = donorService;
        this.donationService = donationService;
    }

    /**
     * Dashboard / Home page showing statistics and key actions.
     */
    @GetMapping("/")
    public String index(Model model) {
        // Simple Donor Statistics (calculated dynamically from database)
        long totalDonors = donorService.getTotalDonorsCount();
        long availableDonors = donorService.getAvailableDonorsCount();
        long cooldownDonors = donorService.getCooldownDonorsCount();
        long totalDonations = donationService.getTotalDonationsCount();

        model.addAttribute("totalDonors", totalDonors);
        model.addAttribute("availableDonors", availableDonors);
        model.addAttribute("cooldownDonors", cooldownDonors);
        model.addAttribute("totalDonations", totalDonations);

        // Donors count by blood group
        model.addAttribute("bloodGroupStats", donorService.getDonorCountByBloodGroup());

        return "index";
    }
}
