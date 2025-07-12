package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.flashcardProgress.FlashcardProgress;
import com.PAP_team_21.flashcards.services.FlashcardProgressService;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flashcardProgress")
@RequiredArgsConstructor
public class FlashcardProgressController {

    private final FlashcardProgressService flashcardProgressService;

    @GetMapping("/getFlashcardProgress/{id}")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getFlashcardProgress(Authentication authentication, @PathVariable int id) {
        try {
            FlashcardProgress flashcardProgress = flashcardProgressService.getFlashcardProgressById(
                    authentication, id);
            return ResponseEntity.ok(flashcardProgress);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
