package com.PAP_team_21.flashcards.services;

import com.PAP_team_21.flashcards.AccessLevel;
import com.PAP_team_21.flashcards.Errors.NoPermissionException;
import com.PAP_team_21.flashcards.UserAnswer;
import com.PAP_team_21.flashcards.authentication.ResourceAccessLevelService.DeckAccessServiceResponse;
import com.PAP_team_21.flashcards.authentication.ResourceAccessLevelService.FlashcardAccessServiceResponse;
import com.PAP_team_21.flashcards.authentication.ResourceAccessLevelService.ResourceAccessService;
import com.PAP_team_21.flashcards.controllers.requests.FlashcardCreationRequest;
import com.PAP_team_21.flashcards.controllers.requests.FlashcardUpdateRequest;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerRepository;
import com.PAP_team_21.flashcards.entities.flashcard.Flashcard;
import com.PAP_team_21.flashcards.entities.flashcard.FlashcardRepository;
import com.PAP_team_21.flashcards.entities.flashcardProgress.FlashcardProgress;
import com.PAP_team_21.flashcards.entities.flashcardProgress.FlashcardProgressRepository;
import com.PAP_team_21.flashcards.entities.reviewLog.ReviewLog;
import com.PAP_team_21.flashcards.entities.reviewLog.ReviewLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FlashcardService {
    private final ResourceAccessService resourceAccessService;
    private final DeckService deckService;
    private final CustomerRepository customerRepository;
    private final FlashcardRepository flashcardRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final FlashcardProgressRepository flashcardProgressRepository;

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

    public Flashcard createFlashcard(Authentication authentication, FlashcardCreationRequest request) {
        DeckAccessServiceResponse response;
        response = resourceAccessService.getDeckAccessLevel(authentication, request.getDeckId(), deckService);

        AccessLevel al = response.getAccessLevel();

        if(al != null && (al.equals(AccessLevel.OWNER) || al.equals(AccessLevel.EDITOR)))
        {
            Flashcard flashcard = new Flashcard(response.getDeck(), request.getFront(), request.getBack());
            this.save(flashcard);

            String email = authentication.getName();
            Optional<Customer> customer = customerRepository.findByEmail(email);
            if (customer.isPresent()) {
                ReviewLog reviewLog = new ReviewLog(flashcard, customer.get(), LocalDateTime.now(), UserAnswer.FORGOT);
                reviewLogRepository.save(reviewLog);
                FlashcardProgress flashcardProgress = new FlashcardProgress(flashcard, customer.get(), LocalDateTime.now(),
                        reviewLog);
                flashcardProgressRepository.save(flashcardProgress);
            }

            return flashcard;
        }
        else {
            throw new NoPermissionException("You do not have permission to create a flashcard here");
        }
    }

    public Flashcard updateFlashcard(Authentication authentication, FlashcardUpdateRequest request) {
        FlashcardAccessServiceResponse response;
        response = resourceAccessService.getFlashcardAccessLevel(authentication, request.getFlashcardId(),
                this);

        AccessLevel al = response.getAccessLevel();

        if(al != null && (al.equals(AccessLevel.OWNER) || al.equals(AccessLevel.EDITOR)))
        {
            Flashcard flashcard = response.getFlashcard();
            flashcard.setFront(request.getFront());
            flashcard.setBack(request.getBack());
            this.save(flashcard);

            return flashcard;
        }
        else {
            throw new NoPermissionException("You do not have permission to update a flashcard here");
        }
    }

    public String deleteFlashcard(Authentication authentication, int flashcardId) {
        FlashcardAccessServiceResponse response;
        response = resourceAccessService.getFlashcardAccessLevel(authentication, flashcardId,
                this);

        AccessLevel al = response.getAccessLevel();

        if(al != null && (al.equals(AccessLevel.OWNER) || al.equals(AccessLevel.EDITOR)))
        {
            this.delete(response.getFlashcard());

            return "deleted successfully";
        }
        else {
            throw new NoPermissionException("You do not have permission to delete a flashcard here");
        }
    }

    public String copyFlashcardToDeck(Authentication authentication, int flashcardId, int deckId) {
        FlashcardAccessServiceResponse flashcardResponse;
        DeckAccessServiceResponse deckResponse;
        flashcardResponse = getFlashcardDeckPair(authentication, flashcardId, deckId).getLeft();
        deckResponse = getFlashcardDeckPair(authentication, flashcardId, deckId).getRight();

        if(!(deckResponse.getAccessLevel().equals(AccessLevel.OWNER) ||
                deckResponse.getAccessLevel().equals(AccessLevel.EDITOR)))
        {
            throw new NoPermissionException("You do not have permission to add to this deck: " + deckId);
        }

        if(!(flashcardResponse.getAccessLevel().equals(AccessLevel.OWNER) ||
                flashcardResponse.getAccessLevel().equals(AccessLevel.EDITOR) ||
                flashcardResponse.getAccessLevel().equals(AccessLevel.VIEWER)))
        {
            throw new NoPermissionException("You do not have permission to add this flashcard");
        }

        Flashcard newFlashcard = new Flashcard(deckResponse.getDeck(),
                flashcardResponse.getFlashcard().getFront(), flashcardResponse.getFlashcard().getBack());
        this.save(newFlashcard);

        return "copied successfully";
    }

    private Pair<FlashcardAccessServiceResponse, DeckAccessServiceResponse> getFlashcardDeckPair(
            Authentication authentication, int flashcardId, int deckId) {
        FlashcardAccessServiceResponse flashcardResponse = resourceAccessService.getFlashcardAccessLevel(
                authentication, flashcardId, this);
        DeckAccessServiceResponse deckResponse = resourceAccessService.getDeckAccessLevel(authentication,
                deckId, deckService);
        return Pair.of(flashcardResponse, deckResponse);
    }

    public String moveFlashcardToOtherDeck(Authentication authentication, int flashcardId, int sourceDeckId,
                                           int destinationDeckId) {
        FlashcardAccessServiceResponse flashcardResponse;
        flashcardResponse = resourceAccessService.getFlashcardAccessLevel(authentication, flashcardId,
                this);

        DeckAccessServiceResponse sourceDeckResponse;
        sourceDeckResponse = resourceAccessService.getDeckAccessLevel(authentication, sourceDeckId,
                deckService);

        DeckAccessServiceResponse destinationDeckResponse;
        destinationDeckResponse = resourceAccessService.getDeckAccessLevel(authentication,
                destinationDeckId, deckService);

        if(!(sourceDeckResponse.getAccessLevel().equals(AccessLevel.OWNER) ||
                sourceDeckResponse.getAccessLevel().equals(AccessLevel.EDITOR)))
        {
            throw new NoPermissionException("You do not have permission to edit this deck: " + sourceDeckId);
        }

        if(!(destinationDeckResponse.getAccessLevel().equals(AccessLevel.OWNER) ||
                destinationDeckResponse.getAccessLevel().equals(AccessLevel.EDITOR)))
        {
            throw new NoPermissionException("You do not have permission to edit this deck: " + destinationDeckId);
        }

        if(!(flashcardResponse.getAccessLevel().equals(AccessLevel.OWNER) ||
                flashcardResponse.getAccessLevel().equals(AccessLevel.EDITOR) ||
                flashcardResponse.getAccessLevel().equals(AccessLevel.VIEWER)))
        {
            throw new NoPermissionException("You do not have permission to move this flashcard");
        }

        if(flashcardResponse.getFlashcard().getDeck().getId() != sourceDeckId)
        {
            throw new IllegalArgumentException("Flashcard is not in source deck");
        }

        flashcardResponse.getFlashcard().setDeck(destinationDeckResponse.getDeck());
        this.save(flashcardResponse.getFlashcard());

        return "flashcard moved!";
    }
}
