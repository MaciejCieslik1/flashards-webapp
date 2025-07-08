package com.PAP_team_21.flashcards.services;

import com.PAP_team_21.flashcards.entities.deck.Deck;
import com.PAP_team_21.flashcards.entities.deck.DeckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class DeckService {
    private final DeckRepository deckRepository;

    @Transactional
    public List<Deck> getLastUsedDecks(int userId, int howMany) {
        return deckRepository.getLastUsed(userId, howMany);
    }

    @Transactional(readOnly = true)
    public Optional<Deck> findById(int deckId) {
        return deckRepository.findById(deckId);
    }

    @Transactional
    public Deck save(Deck deck) {
        return deckRepository.save(deck);
    }

    @Transactional
    public void delete(Deck deck) {
        deckRepository.delete(deck);
    }

    public int countDeckTotalCards(Integer customerId, int deckId) {
        return deckRepository.countDeckAllCards(customerId, deckId);
    }


    public int countDeckNewCards(Integer customerId, int deckId) {
        return deckRepository.countDeckNewCards(customerId, deckId);
    }

    public int countDeckAllDueCards(Integer customerId, int deckId) {
        return deckRepository.countDeckAllDueCards(customerId, deckId);
    }

}
