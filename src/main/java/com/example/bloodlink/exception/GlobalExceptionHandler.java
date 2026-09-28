package com.example.bloodlink.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("title", "Resource Not Found");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("statusCode", 404);
        return "error";
    }

    @ExceptionHandler(CooldownViolationException.class)
    public String handleCooldownViolation(CooldownViolationException ex, Model model) {
        model.addAttribute("title", "Cooldown Period Active");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("eligibleDate", ex.getFormattedEligibleDate());
        model.addAttribute("statusCode", 400);
        return "error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
        model.addAttribute("title", "Invalid Request");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("statusCode", 400);
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        model.addAttribute("title", "An Error Occurred");
        model.addAttribute("errorMessage", "An unexpected error occurred while processing your request. Please try again.");
        model.addAttribute("statusCode", 500);
        return "error";
    }
}
