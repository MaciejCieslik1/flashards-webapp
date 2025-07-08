package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.controllers.requests.UserPreferencesUpdateRequest;
import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerRepository;
import com.PAP_team_21.flashcards.services.CustomerService;
import com.PAP_team_21.flashcards.entities.userPreferences.UserPreferences;
import com.PAP_team_21.flashcards.entities.userPreferences.UserPreferencesRepository;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/userPreferences")
@RequiredArgsConstructor
public class UserPreferencesController {

    private final UserPreferencesRepository userPreferencesRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;

    @GetMapping("/getUserPreferences")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getUserPreferences(Authentication authentication) {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        UserPreferences userPreferences = customer.getUserPreferences();

        return ResponseEntity.ok(userPreferences);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateUserPreferences(Authentication authentication,
                                                   @RequestBody UserPreferencesUpdateRequest request) {
        String email = authentication.getName();
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);
        if (customerOpt.isEmpty())
        {
            return ResponseEntity.badRequest().body("No user with this id found");
        }
        Customer customer = customerOpt.get();

        UserPreferences userPreferences = customer.getUserPreferences();

        userPreferences.setDarkMode(request.isDarkMode());
        userPreferences.setLanguage(request.getLanguage());
        userPreferences.setReminderTime(request.getReminderTime());
        userPreferences.setTimezone(request.getTimezone());
        userPreferences.setStudyReminders(request.getStudyReminders());
        userPreferencesRepository.save(userPreferences);
        return ResponseEntity.ok(userPreferences);
    }
}
