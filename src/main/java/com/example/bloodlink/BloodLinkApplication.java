package com.example.bloodlink;

import com.example.bloodlink.model.BloodGroup;
import com.example.bloodlink.model.DonationRecord;
import com.example.bloodlink.model.Donor;
import com.example.bloodlink.repository.BloodGroupRepository;
import com.example.bloodlink.repository.DonationRecordRepository;
import com.example.bloodlink.repository.DonorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class BloodLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(BloodLinkApplication.class, args);
    }

    /**
     * Seeds initial blood groups so donors can select their blood group upon registration.
     * No demo donors or donation records are inserted; data is only stored when input by the user.
     */
    @Bean
    public CommandLineRunner initData(BloodGroupRepository bloodGroupRepository) {
        return args -> {
            String[] groups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
            for (String groupName : groups) {
                if (!bloodGroupRepository.existsByGroupName(groupName)) {
                    bloodGroupRepository.save(new BloodGroup(groupName));
                }
            }
            System.out.println(">>> Blood groups initialized: A+, A-, B+, B-, AB+, AB-, O+, O-");
        };
    }
}
