package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.AccessLevel;
import com.PAP_team_21.flashcards.Errors.ResourceNotFoundException;
import com.PAP_team_21.flashcards.UserAnswer;
import com.PAP_team_21.flashcards.authentication.ResourceAccessLevelService.DeckAccessServiceResponse;
import com.PAP_team_21.flashcards.authentication.ResourceAccessLevelService.FlashcardAccessServiceResponse;
import com.PAP_team_21.flashcards.authentication.ResourceAccessLevelService.ResourceAccessService;
import com.PAP_team_21.flashcards.controllers.requests.FlashcardCreationRequest;
import com.PAP_team_21.flashcards.controllers.requests.FlashcardUpdateRequest;
import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerRepository;
import com.PAP_team_21.flashcards.entities.deck.DeckService;
import com.PAP_team_21.flashcards.entities.flashcard.Flashcard;
import com.PAP_team_21.flashcards.entities.flashcard.FlashcardRepository;
import com.PAP_team_21.flashcards.entities.flashcard.FlashcardService;
import com.PAP_team_21.flashcards.entities.flashcardProgress.FlashcardProgress;
import com.PAP_team_21.flashcards.entities.flashcardProgress.FlashcardProgressRepository;
import com.PAP_team_21.flashcards.entities.folder.FolderJpaRepository;
import com.PAP_team_21.flashcards.entities.reviewLog.ReviewLog;
import com.PAP_team_21.flashcards.entities.reviewLog.ReviewLogRepository;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/flashcard")
@RequiredArgsConstructor
public class FlashcardController {
    private final CustomerRepository customerRepository;
    private final DeckService deckService;
    private final FolderJpaRepository folderRepository;
    private final FlashcardProgressRepository flashcardProgressRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final FlashcardService flashcardService;

    private final ResourceAccessService resourceAccessService;

    // create flashcard
    @PostMapping("/create")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> createFlashcard(
            Authentication authentication,
            @RequestBody FlashcardCreationRequest request)
    {
        DeckAccessServiceResponse response;

        try{
            response = resourceAccessService.getDeckAccessLevel(authentication, request.getDeckId());
        } catch (ResourceNotFoundException e)
        {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        AccessLevel al = response.getAccessLevel();

        if(al != null && (al.equals(AccessLevel.OWNER) || al.equals(AccessLevel.EDITOR)))
        {
            Flashcard flashcard = new Flashcard(response.getDeck(), request.getFront(), request.getBack());
            flashcardService.save(flashcard);

            String email = authentication.getName();
            Optional<Customer> customer = customerRepository.findByEmail(email);
            if (customer.isPresent()) {
                ReviewLog reviewLog = new ReviewLog(flashcard, customer.get(), LocalDateTime.now(), UserAnswer.FORGOT);
                reviewLogRepository.save(reviewLog);
                FlashcardProgress flashcardProgress = new FlashcardProgress(flashcard, customer.get(), LocalDateTime.now(),
                        reviewLog);
                flashcardProgressRepository.save(flashcardProgress);
            }

            return ResponseEntity.ok(flashcard);
        }

        return ResponseEntity.badRequest().body("You do not have permission to create a deck here");
    }
    // update flashcard
    @PostMapping("/update")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> updateFlashcard(
            Authentication authentication,
            @RequestBody FlashcardUpdateRequest request)
    {
        FlashcardAccessServiceResponse response;
        try{
            response = resourceAccessService.getFlashcardAccessLevel(authentication, request.getFlashcardId());
        } catch (ResourceNotFoundException e)
        {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        AccessLevel al = response.getAccessLevel();

        if(al != null && (al.equals(AccessLevel.OWNER) || al.equals(AccessLevel.EDITOR)))
        {
            Flashcard flashcard = response.getFlashcard();
            flashcard.setFront(request.getFront());
            flashcard.setBack(request.getBack());
            flashcardService.save(flashcard);

            return ResponseEntity.ok(flashcard);
        }

        return ResponseEntity.badRequest().body("You do not have permission to create a deck here");
    }

    // delete
    @DeleteMapping("/delete")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> updateFlashcard(
            Authentication authentication,
            @RequestParam int flashcardId)
    {

        FlashcardAccessServiceResponse response;
        try{
            response = resourceAccessService.getFlashcardAccessLevel(authentication, flashcardId);
        } catch (ResourceNotFoundException e)
        {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        AccessLevel al = response.getAccessLevel();

        if(al != null && (al.equals(AccessLevel.OWNER) || al.equals(AccessLevel.EDITOR)))
        {
            flashcardService.delete(response.getFlashcard());

            return ResponseEntity.ok("deleted successfully");
        }

        return ResponseEntity.badRequest().body("You do not have permission to create a deck here");
    }


    @PostMapping("/copyFlashcardToDeck")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> addFlashcardToDeck(
            Authentication authentication,
            @RequestParam() int deckId,
            @RequestParam() int flashcardId
    )
    {
        FlashcardAccessServiceResponse flashcardResponse;
        try{
            flashcardResponse = resourceAccessService.getFlashcardAccessLevel(authentication, flashcardId);
        } catch (ResourceNotFoundException e)
        {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        DeckAccessServiceResponse deckResponse;
        try{
            deckResponse = resourceAccessService.getDeckAccessLevel(authentication, deckId);
        } catch (ResourceNotFoundException e)
        {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        if(!(deckResponse.getAccessLevel().equals(AccessLevel.OWNER) ||
                deckResponse.getAccessLevel().equals(AccessLevel.EDITOR)))
        {
            return ResponseEntity.badRequest().body("You do not have permission to add to this deck: " + deckId);
        }

        if(!(flashcardResponse.getAccessLevel().equals(AccessLevel.OWNER) ||
                flashcardResponse.getAccessLevel().equals(AccessLevel.EDITOR) ||
                flashcardResponse.getAccessLevel().equals(AccessLevel.VIEWER)))
        {
            return ResponseEntity.badRequest().body("You do not have permission to add this flashcard");
        }

        Flashcard newFlashcard = new Flashcard(deckResponse.getDeck(), flashcardResponse.getFlashcard().getFront(), flashcardResponse.getFlashcard().getBack());
        flashcardService.save(newFlashcard);

        return ResponseEntity.ok("copied successfuly");
    }

    @PostMapping("/moveFlashcardToOtherDeck")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> moveFlashcardToOtherDeck(
            Authentication authentication,
            @RequestParam() int sourceDeckId,
            @RequestParam() int destinationDeckId,
            @RequestParam() int flashcardId
    )
    {
        FlashcardAccessServiceResponse flashcardResponse;
        try{
            flashcardResponse = resourceAccessService.getFlashcardAccessLevel(authentication, flashcardId);
        } catch (ResourceNotFoundException e)
        {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        DeckAccessServiceResponse sourceDeckResponse;
        try{
            sourceDeckResponse = resourceAccessService.getDeckAccessLevel(authentication, sourceDeckId);
        } catch (ResourceNotFoundException e)
        {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        DeckAccessServiceResponse destinationDeckResponse;
        try{
            destinationDeckResponse = resourceAccessService.getDeckAccessLevel(authentication, destinationDeckId);
        } catch (ResourceNotFoundException e)
        {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        if(!(sourceDeckResponse.getAccessLevel().equals(AccessLevel.OWNER) ||
                sourceDeckResponse.getAccessLevel().equals(AccessLevel.EDITOR)))
        {
            return ResponseEntity.badRequest().body("You do not have permission to edit this deck: " + sourceDeckId);
        }

        if(!(destinationDeckResponse.getAccessLevel().equals(AccessLevel.OWNER) ||
                destinationDeckResponse.getAccessLevel().equals(AccessLevel.EDITOR)))
        {
            return ResponseEntity.badRequest().body("You do not have permission to edit this deck: " + destinationDeckId);
        }

        if(!(flashcardResponse.getAccessLevel().equals(AccessLevel.OWNER) ||
                flashcardResponse.getAccessLevel().equals(AccessLevel.EDITOR) ||
                flashcardResponse.getAccessLevel().equals(AccessLevel.VIEWER)))
        {
            return ResponseEntity.badRequest().body("You do not have permission to move this flashcard");
        }

        if(flashcardResponse.getFlashcard().getDeck().getId() != sourceDeckId)
        {
            return ResponseEntity.badRequest().body("Flashcard is not in source deck");
        }

        flashcardResponse.getFlashcard().setDeck(destinationDeckResponse.getDeck());
        flashcardService.save(flashcardResponse.getFlashcard());

        return ResponseEntity.ok("flashcard moved!");
    }
}
