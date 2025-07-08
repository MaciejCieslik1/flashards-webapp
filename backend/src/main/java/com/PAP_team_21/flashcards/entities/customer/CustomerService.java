package com.PAP_team_21.flashcards.entities.customer;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerDao customerDaoImpl;
    private final CustomerRepository customerRepository;

    public List<Customer> findByUsername(String username) {
        return customerDaoImpl.findUserByUsername(username);
    }

    public Customer checkForLoggedCustomer(Authentication authentication) {
        String email = authentication.getName();
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer with email " + email + " not found"));
    }
}
