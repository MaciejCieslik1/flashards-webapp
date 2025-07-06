package com.PAP_team_21.flashcards.entities.customer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerDao customerDaoImpl;

    public List<Customer> findByUsername(String username) {
        return customerDaoImpl.findUserByUsername(username);
    }
}
