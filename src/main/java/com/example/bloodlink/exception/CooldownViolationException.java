package com.example.bloodlink.exception;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CooldownViolationException extends RuntimeException {

    private final LocalDate nextEligibleDate;

    public CooldownViolationException(String message, LocalDate nextEligibleDate) {
        super(message);
        this.nextEligibleDate = nextEligibleDate;
    }

    public LocalDate getNextEligibleDate() {
        return nextEligibleDate;
    }

    public String getFormattedEligibleDate() {
        if (nextEligibleDate == null) {
            return "N/A";
        }
        return nextEligibleDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
    }
}
