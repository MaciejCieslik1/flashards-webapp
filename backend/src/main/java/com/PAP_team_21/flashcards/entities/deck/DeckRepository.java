package com.PAP_team_21.flashcards.entities.deck;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public  interface DeckRepository extends JpaRepository<Deck, Integer> {

    @Procedure(procedureName = "get_last_used_decks")
    List<Deck> getLastUsed(@Param("userId") int userId, @Param("howMany") int howMany);

    @Procedure(procedureName = "count_decks_new_cards")
    int countDeckNewCards(@Param("userId")int customerId, @Param("deckId") int deckId);

    @Procedure(procedureName = "count_all_deck_cards")
    int countDeckAllCards(@Param("userId")int customerId, @Param("deckId") int deckId);

    @Procedure(procedureName = "count_all_deck_due_cards")
    int countDeckAllDueCards(@Param("userId")int customerId, @Param("deckId") int deckId);
}