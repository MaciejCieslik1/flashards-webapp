package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.Errors.ResourceNotFoundException;
import com.PAP_team_21.flashcards.ReviewService.ReviewService;
import com.PAP_team_21.flashcards.authentication.ResourceAccessLevelService.DeckAccessServiceResponse;
import com.PAP_team_21.flashcards.authentication.ResourceAccessLevelService.FlashcardAccessServiceResponse;
import com.PAP_team_21.flashcards.authentication.ResourceAccessLevelService.ResourceAccessService;
import com.PAP_team_21.flashcards.controllers.requests.FlashcardsReviewedRequest;
import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final ResourceAccessService resourceAccessService;

    @GetMapping("/reviewDeck")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> reviewDeck(Authentication authentication,
                                        @RequestParam int deckId,
                                        @RequestParam(defaultValue = "10") int batchSize) {
        if (batchSize < 10)
            return ResponseEntity.badRequest().body("Batch size must be at least 10");

        DeckAccessServiceResponse response;
        try {
            response = resourceAccessService.getDeckAccessLevel(authentication, deckId);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        if (response.getAccessLevel() == null) {
            return ResponseEntity.badRequest().body("You do not have access to this deck");
        }

        return ResponseEntity.ok(reviewService.reviewDeck(response.getCustomer(), response.getDeck(), batchSize));
    }

    @PostMapping("/flashcardReviewed")
    public ResponseEntity<?> flashcardReviewed(Authentication authentication,
                                               @RequestBody FlashcardsReviewedRequest reviewResponse) {
        FlashcardAccessServiceResponse response;
        try {
            response = resourceAccessService.getFlashcardAccessLevel(authentication, reviewResponse.getFlashcardId());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        if (response.getAccessLevel() == null) {
            return ResponseEntity.badRequest().body("You do not have access to this flashcard");
        }

        reviewService.flashcardReviewed(response.getCustomer(), response.getFlashcard(), reviewResponse.getUserAnswer());
        return ResponseEntity.ok("Flashcard reviewed");
    }
}
