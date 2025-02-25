package com.PAP_team_21.flashcards.controllers.DTOMappers;

import com.PAP_team_21.flashcards.controllers.DTO.DeckDTO;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.deck.Deck;
import com.PAP_team_21.flashcards.entities.deck.DeckService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeckMapper {
    private final DeckService deckService;


    public DeckDTO toDTO(Customer customer, Deck deck) {
        int totalCards = deckService.countDeckTotalCards(customer.getId(), deck.getId());
        int newCards = deckService.countDeckNewCards(customer.getId(), deck.getId());
        int toReview = deckService.countDeckAllDueCards(customer.getId(), deck.getId());
        int learnedCards = totalCards - newCards - toReview;

        float progress = (float)learnedCards / (float) totalCards * 100;

        return new DeckDTO(deck.getId(), deck.getName(), progress, newCards, toReview, totalCards, learnedCards);
    }

    public List<DeckDTO> toDTO(Customer customer, List<Deck> decks) {
        List<DeckDTO> result = new ArrayList<>();
        for(Deck d: decks)
        {
            result.add(toDTO(customer, d));
        }

        return result;
    }
}
