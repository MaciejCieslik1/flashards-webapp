package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.AccessLevel;
import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerRepository;
import com.PAP_team_21.flashcards.entities.customer.CustomerService;
import com.PAP_team_21.flashcards.entities.deck.Deck;
import com.PAP_team_21.flashcards.entities.flashcard.Flashcard;
import com.PAP_team_21.flashcards.entities.reviewLog.ReviewLog;
import com.PAP_team_21.flashcards.entities.reviewLog.ReviewLogRepository;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/reviewLog")
@RequiredArgsConstructor
public class ReviewLogController {

    private final ReviewLogRepository reviewLogRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;

    @GetMapping("/getReviewLog/{id}")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getReviewLog(Authentication authentication, @PathVariable int id) {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        Optional<ReviewLog> reviewLogOpt = reviewLogRepository.findById(id);
        if(reviewLogOpt.isEmpty())
        {
            return ResponseEntity.badRequest().body("No reviewLog with this id found");
        }
        ReviewLog reviewLog = reviewLogOpt.get();

        Flashcard flashcard = reviewLog.getFlashcard();
        Deck deck = flashcard.getDeck();
        AccessLevel al = deck.getAccessLevel(customer);
        if (al == null)
        {
            return ResponseEntity.badRequest().body("You dont have access to this flashcard");
        }

        return ResponseEntity.ok(reviewLog);
    }

    @GetMapping("/getAllReviewLogs")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getAllReviewLogs(Authentication authentication) {
        String email = authentication.getName();
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);
        if(customerOpt.isEmpty())
        {
            return ResponseEntity.badRequest().body("No user with this id found");
        }
        Customer customer = customerOpt.get();

        List<ReviewLog> reviewLogs = customer.getReviewLogs();

        return ResponseEntity.ok(reviewLogs);
    }


    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteReviewLog(Authentication authentication,
                                                @RequestParam int reviewLogId) {
        String email = authentication.getName();
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);
        if (customerOpt.isEmpty())
        {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        Optional<ReviewLog> reviewLogOpt = reviewLogRepository.findById(reviewLogId);
        if (reviewLogOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("No reviewLog with this id found");
        }
        ReviewLog reviewLog = reviewLogOpt.get();

        if (reviewLog.getUserId() != customerOpt.get().getId()) {
            return ResponseEntity.badRequest().body("This reviewLog does not belong to user");
        }

        reviewLogRepository.delete(reviewLog);
        return ResponseEntity.ok("ReviewLog successfully deleted");
    }
}
