package com.PAP_team_21.flashcards.services;

import com.PAP_team_21.flashcards.AccessLevel;
import com.PAP_team_21.flashcards.Errors.NoPermissionException;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.deck.Deck;
import com.PAP_team_21.flashcards.entities.flashcard.Flashcard;
import com.PAP_team_21.flashcards.entities.flashcardProgress.FlashcardProgress;
import com.PAP_team_21.flashcards.entities.flashcardProgress.FlashcardProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FlashcardProgressService {

    private final CustomerService customerService;
    private final FlashcardProgressRepository flashcardProgressRepository;

    public FlashcardProgress getFlashcardProgressById(Authentication authentication, int id) {
        Customer customer = customerService.checkForLoggedCustomer(authentication);

        Optional<FlashcardProgress> flashcardProgressOpt = flashcardProgressRepository.findById(id);
        if (flashcardProgressOpt.isEmpty()) {
            throw new IllegalArgumentException("No flashcard progress with this id found");
        }

        FlashcardProgress flashcardProgress = flashcardProgressOpt.get();
        Flashcard flashcard = flashcardProgress.getFlashcard();
        Deck deck = flashcard.getDeck();

        AccessLevel al = deck.getAccessLevel(customer);
        if (al == null)
        {
            throw new NoPermissionException("You dont have access to this flashcard");
        }

        return flashcardProgress;

    }
}
