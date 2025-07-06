package com.PAP_team_21.flashcards.entities.flashcard;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FlashcardService {
    private final FlashcardRepository flashcardRepository;


    public Optional<Flashcard> findById(int flashcardId) {
        return flashcardRepository.findById(flashcardId);
    }

    public void save(Flashcard flashcard) {
        flashcardRepository.save(flashcard);
    }

    public void delete(Flashcard flashcard) {
    flashcardRepository.delete(flashcard);
    }

    public int countCurrentlyLearning(Integer customerId, int deckId, Duration reviewGapConstant, Duration lastReviewConstant) {
        return flashcardRepository.countCurrentlyLearning( customerId,  deckId,  (int)reviewGapConstant.toMinutes(),  (int)lastReviewConstant.toMinutes());
    }

    public int countDueInLearning(Integer customerId, int deckId, Duration reviewGapConstant, Duration lastReviewConstant) {
        return flashcardRepository.countDueInLearning(customerId, deckId, (int)reviewGapConstant.toMinutes(), (int)lastReviewConstant.toMinutes());
    }

    public int countDueToReview(Integer customerId, int deckId, Duration reviewGapConstant, Duration lastReviewConstant) {
        return flashcardRepository.countDueToReview(customerId, deckId, (int)reviewGapConstant.toMinutes(), (int)lastReviewConstant.toMinutes());
    }

    public List<Flashcard> getDueFlashcards(Integer customerId, int deckId, int howMany) {
        return flashcardRepository.getDueFlashcards(customerId, deckId, howMany);
    }

    public List<Flashcard> getNewFlashcards(int customerId, int deckId, int howManyNewToAdd) {
        return flashcardRepository.getNewFlashcards(customerId, deckId, howManyNewToAdd);
    }

    public List<Flashcard> getEarlyReviewFlashcards(Integer customerId, int deckId, Duration reviewGapConstant, Duration lastReviewConstant, int howMany) {
        return flashcardRepository.getEarlyReviewFlashcards(customerId, deckId, (int)reviewGapConstant.toMinutes(), (int)lastReviewConstant.toMinutes(), howMany);
    }

    public List<Flashcard> getDueInLearning(Integer customerId, int deckId, Duration reviewGapConstant, Duration lastReviewConstant, int howMany) {
        return flashcardRepository.getDueInLearning(customerId, deckId, (int)reviewGapConstant.toMinutes(), (int)lastReviewConstant.toMinutes(), howMany);
    }

    public List<Flashcard> getDueToReview(Integer customerId, int deckId, Duration reviewGapConstant, Duration lastReviewConstant, int howMany) {
        return flashcardRepository.getDueToReview(customerId, deckId, (int)reviewGapConstant.toMinutes(), (int)lastReviewConstant.toMinutes(), howMany);
    }

    @Transactional
    public int countAllNewCards(int customerId)
    {
        return flashcardRepository.countAllNewCards(customerId);
    }

    @Transactional
    public int countAllDueCards(int customerId)
    {
        return flashcardRepository.countAllDueCards(customerId);
    }

    @Transactional
    public int countAllCards(int customerId)
    {
        return flashcardRepository.countAllCards(customerId);
    }
}
