package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.controllers.requests.FlashcardCreationRequest;
import com.PAP_team_21.flashcards.controllers.requests.FlashcardUpdateRequest;
import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.flashcard.Flashcard;
import com.PAP_team_21.flashcards.services.FlashcardService;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flashcard")
@RequiredArgsConstructor
public class FlashcardController {
    private final FlashcardService flashcardService;

    @PostMapping("/create")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> createFlashcard(
            Authentication authentication,
            @RequestBody FlashcardCreationRequest request) {
        try {
            Flashcard flashcard = flashcardService.createFlashcard(authentication, request);
            return ResponseEntity.ok(flashcard);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/update")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> updateFlashcard(
            Authentication authentication,
            @RequestBody FlashcardUpdateRequest request) {
        try {
            Flashcard flashcard = flashcardService.updateFlashcard(authentication, request);
            return ResponseEntity.ok(flashcard);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> updateFlashcard(
            Authentication authentication,
            @RequestParam int flashcardId) {
        try {
            String communicate = flashcardService.deleteFlashcard(authentication, flashcardId);
            return ResponseEntity.ok(communicate);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/copyFlashcardToDeck")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> addFlashcardToDeck(
            Authentication authentication,
            @RequestParam() int deckId,
            @RequestParam() int flashcardId) {
        try {
            String communicate = flashcardService.copyFlashcardToDeck(authentication, deckId, flashcardId);
            return ResponseEntity.ok(communicate);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/moveFlashcardToOtherDeck")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> moveFlashcardToOtherDeck(
            Authentication authentication,
            @RequestParam() int sourceDeckId,
            @RequestParam() int destinationDeckId,
            @RequestParam() int flashcardId) {
        try {
            String communicate = flashcardService.moveFlashcardToOtherDeck(authentication, sourceDeckId,
                    destinationDeckId, flashcardId);
            return ResponseEntity.ok(communicate);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
