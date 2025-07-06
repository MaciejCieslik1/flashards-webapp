package com.PAP_team_21.flashcards.entities.flashcard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FlashcardRepository extends JpaRepository<Flashcard, Integer> {

    @Procedure(procedureName = "count_current_learning")
    int countCurrentlyLearning(@Param("userId")Integer customerId,
                               @Param("deckId") int deckId,
                               @Param("revGapMinutes")int reviewGapConstantMinutes,
                               @Param("lastReviewConstant")int lastReviewConstantMinutes);

    @Procedure(procedureName = "count_due_in_learning")
    int countDueInLearning(@Param("userId")Integer customerId,
                           @Param("deckId") int deckId,
                           @Param("revGapMinutes")int reviewGapConstantMinutes,
                           @Param("lastReviewConstant")int lastReviewConstantMinutes);

    @Procedure(procedureName = "count_due_to_review")
    int countDueToReview(@Param("userId")Integer customerId,
                         @Param("deckId") int deckId,
                         @Param("revGapMinutes")int reviewGapConstantMinutes,
                         @Param("lastReviewConstant")int lastReviewConstantMinutes);

    @Procedure(procedureName = "get_due_flashcards")
    List<Flashcard> getDueFlashcards(@Param("userId")Integer customerId,
                                     @Param("deckId") int deckId,
                                     @Param("howMany")int howMany);

    @Procedure(procedureName = "get_new_flashcard")
    List<Flashcard> getNewFlashcards(@Param("userId")Integer customerId,
                                     @Param("deckId") int deckId,
                                     @Param("howMany")int howMany);

    @Procedure(procedureName = "get_early_review")
    List<Flashcard> getEarlyReviewFlashcards(@Param("userId")Integer customerId,
                                             @Param("deckId") int deckId,
                                             @Param("revGapMinutes")int reviewGapConstantMinutes,
                                             @Param("lastReviewConstant")int lastReviewConstantMinutes,
                                             @Param("howMany")int howMany);

    @Procedure(procedureName = "get_due_in_learning")
    List<Flashcard> getDueInLearning(@Param("userId")Integer customerId,
                                     @Param("deckId") int deckId,
                                     @Param("revGapMinutes")int reviewGapConstantMinutes,
                                     @Param("lastReviewConstant")int lastReviewConstantMinutes,
                                     @Param("howMany")int howMany);

    @Procedure(procedureName = "get_due_to_review")
    List<Flashcard> getDueToReview(@Param("userId")Integer customerId,
                                   @Param("deckId") int deckId,
                                   @Param("revGapMinutes")int reviewGapConstantMinutes,
                                   @Param("lastReviewConstant")int lastReviewConstantMinutes,
                                   @Param("howMany")int howMany);

    @Procedure(procedureName = "count_all_new_cards")
    int countAllNewCards(@Param("userId")int customerId);

    @Procedure(procedureName = "count_all_due_cards")
    int countAllDueCards(@Param("userId")int customerId);


    @Procedure(procedureName = "count_all_cards")
    int countAllCards(@Param("userId")int customerId);
}


