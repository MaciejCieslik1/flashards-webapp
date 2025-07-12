package com.PAP_team_21.flashcards.services;

import com.PAP_team_21.flashcards.AccessLevel;
import com.PAP_team_21.flashcards.Errors.NoPermissionException;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerRepository;
import com.PAP_team_21.flashcards.entities.deck.Deck;
import com.PAP_team_21.flashcards.entities.flashcard.Flashcard;
import com.PAP_team_21.flashcards.entities.reviewLog.ReviewLog;
import com.PAP_team_21.flashcards.entities.reviewLog.ReviewLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewLogService {

    private final CustomerService customerService;
    private final ReviewLogRepository reviewLogRepository;
    private final CustomerRepository customerRepository;

    public ReviewLog getReviewLogById(Authentication authentication, int id) {
        Customer customer = customerService.checkForLoggedCustomer(authentication);
        Optional<ReviewLog> reviewLogOpt = reviewLogRepository.findById(id);
        if(reviewLogOpt.isEmpty()) {
            throw new IllegalArgumentException("No reviewLog with this id found");
        }
        ReviewLog reviewLog = reviewLogOpt.get();
        Flashcard flashcard = reviewLog.getFlashcard();
        Deck deck = flashcard.getDeck();
        AccessLevel al = deck.getAccessLevel(customer);
        if (al == null) {
            throw new NoPermissionException("You dont have access to this flashcard");
        }

        return reviewLog;
    }

    public List<ReviewLog> getAllReviewLogs(Authentication authentication) {
        String email = authentication.getName();
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);

        if(customerOpt.isEmpty()) {
            throw new IllegalArgumentException("No user with this id found");
        }
        Customer customer = customerOpt.get();
        return customer.getReviewLogs();
    }

    public String deleteReviewLogById(Authentication authentication, int id) {
        String email = authentication.getName();
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);
        if (customerOpt.isEmpty()) {
            throw new IllegalArgumentException("No user with this id found");
        }

        Optional<ReviewLog> reviewLogOpt = reviewLogRepository.findById(id);
        if (reviewLogOpt.isEmpty()) {
            throw new IllegalArgumentException("No reviewLog with this id found");
        }
        ReviewLog reviewLog = reviewLogOpt.get();

        if (reviewLog.getUserId() != customerOpt.get().getId()) {
            throw new NoPermissionException("This reviewLog does not belong to user");
        }

        reviewLogRepository.delete(reviewLog);
        return "ReviewLog successfully deleted";
    }
}
