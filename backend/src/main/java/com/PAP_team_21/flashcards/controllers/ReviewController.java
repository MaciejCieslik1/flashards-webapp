package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.entities.flashcard.Flashcard;
import com.PAP_team_21.flashcards.services.ReviewService;
import com.PAP_team_21.flashcards.controllers.requests.FlashcardsReviewedRequest;
import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/reviewDeck")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> reviewDeck(Authentication authentication,
                                        @RequestParam int deckId,
                                        @RequestParam(defaultValue = "10") int batchSize) {
        try {
            List<Flashcard> reviewFlashcards = reviewService.reviewDeck(authentication, deckId, batchSize);
            return ResponseEntity.ok(reviewFlashcards);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/flashcardReviewed")
    public ResponseEntity<?> flashcardReviewed(Authentication authentication,
                                               @RequestBody FlashcardsReviewedRequest reviewResponse) {
        try {
            String communicate = reviewService.FlashcardReviewed(authentication, reviewResponse);
            return ResponseEntity.ok(communicate);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
