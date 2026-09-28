package com.example.bloodlink.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(name = "donation_record")
public class DonationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Donor is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    @NotNull(message = "Donation date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "donation_date", nullable = false)
    private LocalDate donationDate;

    @NotBlank(message = "Hospital name cannot be empty")
    @Column(name = "hospital_name", nullable = false, length = 150)
    private String hospitalName;

    @Column(length = 255)
    private String notes;

    public DonationRecord() {
    }

    public DonationRecord(Donor donor, LocalDate donationDate, String hospitalName, String notes) {
        this.donor = donor;
        this.donationDate = donationDate;
        this.hospitalName = hospitalName;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Donor getDonor() {
        return donor;
    }

    public void setDonor(Donor donor) {
        this.donor = donor;
    }

    public LocalDate getDonationDate() {
        return donationDate;
    }

    public void setDonationDate(LocalDate donationDate) {
        this.donationDate = donationDate;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
