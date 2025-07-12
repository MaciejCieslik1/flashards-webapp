package com.PAP_team_21.flashcards.services;

import com.PAP_team_21.flashcards.controllers.requests.UserPreferencesUpdateRequest;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerRepository;
import com.PAP_team_21.flashcards.entities.userPreferences.UserPreferences;
import com.PAP_team_21.flashcards.entities.userPreferences.UserPreferencesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserPreferencesService {
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;
    private final UserPreferencesRepository userPreferencesRepository;

    public UserPreferences getUserPreferences(Authentication authentication) {
        Customer customer = customerService.checkForLoggedCustomer(authentication);

        return customer.getUserPreferences();
    }

    public UserPreferences updateUserPreferences(Authentication authentication,
                                                 UserPreferencesUpdateRequest request) {
        String email = authentication.getName();
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);

        if (customerOpt.isEmpty()) {
            throw new IllegalArgumentException("No user with this id found");
        }

        Customer customer = customerOpt.get();
        UserPreferences userPreferences = customer.getUserPreferences();
        userPreferences.setDarkMode(request.isDarkMode());
        userPreferences.setLanguage(request.getLanguage());
        userPreferences.setReminderTime(request.getReminderTime());
        userPreferences.setTimezone(request.getTimezone());
        userPreferences.setStudyReminders(request.getStudyReminders());
        userPreferencesRepository.save(userPreferences);
        return userPreferences;
    }
}
