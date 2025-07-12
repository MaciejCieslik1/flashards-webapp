package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.Errors.ResourceNotFoundException;
import com.PAP_team_21.flashcards.controllers.DTO.DeckDTO;
import com.PAP_team_21.flashcards.controllers.requests.DeckCreationRequest;
import com.PAP_team_21.flashcards.controllers.requests.DeckUpdateRequest;
import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.flashcard.Flashcard;
import com.PAP_team_21.flashcards.entities.deck.Deck;
import com.PAP_team_21.flashcards.services.DeckService;
import com.PAP_team_21.flashcards.services.FolderService;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/deck")
@RequiredArgsConstructor
public class DeckController {
    private final DeckService deckService;
    private final FolderService folderService;

    @GetMapping("/flashcards")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getFlashcards(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending,
            @RequestParam() int deckId) {
        try {
            List<Flashcard> flashcards = deckService.getFlashcards(authentication, deckId, page, size,
                    sortBy, ascending);
            return ResponseEntity.ok(flashcards);
        }
        catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getLastUsed")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getLastUsed(
            Authentication authentication,
            @RequestParam(defaultValue = "3") int howMany) {
        try {
            List<DeckDTO> deckDTOs = deckService.getLastUsed(authentication, howMany);
            return ResponseEntity.ok(deckDTOs);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getAllDecks")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getAllDecks(Authentication authentication)
    {
        try {
            List<Deck> decks = deckService.getAllDecks(authentication, folderService);
            return ResponseEntity.ok(decks);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e);
        }
    }

    @GetMapping("/getAllDecksInfo")
    public ResponseEntity<?> getAllDecksInfo(Authentication authentication)
    {
        try {
            List<DeckDTO> deckDTOs = deckService.getAllDecksInfo(authentication, folderService);
            return ResponseEntity.ok(deckDTOs);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e);
        }
    }

    @PostMapping("/create")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> createDeck(
            Authentication authentication,
            @RequestBody DeckCreationRequest request) {
        try {
            DeckDTO deckDTO = deckService.createDeck(authentication, request);
            return ResponseEntity.ok(deckDTO);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/update")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> updateDeck(
            Authentication authentication,
            @RequestBody DeckUpdateRequest request) {
        try {
            DeckDTO deckDTO = deckService.updateDeck(authentication, request);
            return ResponseEntity.ok(deckDTO);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> deleteDeck(
            Authentication authentication,
            @RequestParam() int deckId) {
        try {
            String communicate = deckService.deleteDeck(authentication, deckId);
            return ResponseEntity.ok(communicate);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getDeck")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getDeckById(Authentication authentication, @RequestParam int deckId)
    {
        try {
            Deck deck = deckService.getDeck(authentication, deckId);
            return ResponseEntity.ok(deck);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getDeckInfo")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getDeckInfoById(Authentication authentication, @RequestParam int deckId)
    {
        try{
            DeckDTO deckDTO = deckService.getDeckInfo(authentication, deckId);
            return ResponseEntity.ok(deckDTO);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
