package com.PAP_team_21.flashcards.entities.customer;

import java.util.List;

public interface CustomerDao {

    void save(Customer customer);

    List<Customer> findUserByUsername(String username);

    void update(Customer customer);

    boolean checkIfEmailAvailable(String email);
}
