package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.AccessLevel;
import com.PAP_team_21.flashcards.Errors.ResourceNotFoundException;
import com.PAP_team_21.flashcards.authentication.ResourceAccessLevelService.DeckAccessServiceResponse;
import com.PAP_team_21.flashcards.authentication.ResourceAccessLevelService.FolderAccessServiceResponse;
import com.PAP_team_21.flashcards.authentication.ResourceAccessLevelService.ResourceAccessService;
import com.PAP_team_21.flashcards.controllers.DTOMappers.DeckMapper;
import com.PAP_team_21.flashcards.controllers.requests.DeckCreationRequest;
import com.PAP_team_21.flashcards.controllers.requests.DeckUpdateRequest;
import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerService;
import com.PAP_team_21.flashcards.entities.deck.Deck;
import com.PAP_team_21.flashcards.entities.deck.DeckService;
import com.PAP_team_21.flashcards.entities.folder.Folder;
import com.PAP_team_21.flashcards.entities.folder.FolderService;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/deck")
@RequiredArgsConstructor
public class DeckController {
    private final ResourceAccessService resourceAccessService;
    private final CustomerService customerService;
    private final DeckService deckService;
    private final DeckMapper deckMapper;
    private final FolderService folderService;

    @GetMapping("/flashcards")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getFlashcards(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending,
            @RequestParam() int deckId)
    {
        DeckAccessServiceResponse response;
        try {
            response = resourceAccessService.getDeckAccessLevel(authentication, deckId);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        AccessLevel al = response.getAccessLevel();
        if(al==null)
        {
            return ResponseEntity.badRequest().body("You dont have access to this deck");
        }
        return ResponseEntity.ok(response.getDeck().getFlashcards( page,  size,  sortBy,  ascending));
    }

    @GetMapping("/getLastUsed")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getLastUsed(
            Authentication authentication,
            @RequestParam(defaultValue = "3") int howMany
    )
    {
        if(howMany<=0)
        {
            return ResponseEntity.badRequest().body("howMany must be greater than 0");
        }

        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        List<Deck> decks = deckService.getLastUsedDecks(customer.getId(), howMany);
        return ResponseEntity.ok(deckMapper.toDTO(customer, decks));
    }

    @GetMapping("/getAllDecks")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getAllDecks(Authentication authentication)
    {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        List<Deck> result = findDecks(customer);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getAllDecksInfo")
    public ResponseEntity<?> getAllDecksInfo(Authentication authentication)
    {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }
        List<Deck> result = findDecks(customer);

        return ResponseEntity.ok(deckMapper.toDTO(customer, result));
    }

    private List<Deck> findDecks(Customer customer) {
        List<Deck> result = new ArrayList<>();
        List<Folder> folders = folderService.findAllUserFolders(customer.getId());

        for(Folder f: folders)
        {
            result.addAll(f.getDecks());
        }
        return result;
    }

    @PostMapping("/create")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> createDeck(
            Authentication authentication,
            @RequestBody DeckCreationRequest request)
    {
        FolderAccessServiceResponse response;
        try{
            response = resourceAccessService.getFolderAccessLevel(authentication, request.getFolderId());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        AccessLevel al = response.getAccessLevel();
        if(al != null && (al.equals(AccessLevel.OWNER) || al.equals(AccessLevel.EDITOR)))
        {
            Deck deck = new Deck(request.getName(), response.getFolder());
            deckService.save(deck);
            return ResponseEntity.ok(deckMapper.toDTO(response.getCustomer(), deck));
        }
        return ResponseEntity.badRequest().body("You do not have permission to create a deck here");
    }

    @PostMapping("/update")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> updateDeck(
            Authentication authentication,
            @RequestBody DeckUpdateRequest request
    )
    {
        DeckAccessServiceResponse response;
        try{
            response = resourceAccessService.getDeckAccessLevel(authentication, request.getDeckId());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        AccessLevel al = response.getAccessLevel();

        if(al != null && (al.equals(AccessLevel.OWNER) || al.equals(AccessLevel.EDITOR)))
        {
            response.getDeck().setName(request.getName());
            deckService.save(response.getDeck());
            return ResponseEntity.ok(deckMapper.toDTO(response.getCustomer(), response.getDeck()));
        }
        return ResponseEntity.badRequest().body("You do not have permission to create a deck here");
    }

    @DeleteMapping("/delete")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> deleteDeck(
            Authentication authentication,
            @RequestParam() int deckId
    )
    {
        DeckAccessServiceResponse response;
        try{
            response = resourceAccessService.getDeckAccessLevel(authentication,deckId);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        AccessLevel al = response.getAccessLevel();

        if(al != null && (al.equals(AccessLevel.OWNER) || al.equals(AccessLevel.EDITOR)))
        {
            deckService.delete(response.getDeck());
            return ResponseEntity.ok("deck deleted");
        }
        return ResponseEntity.badRequest().body("You do not have permission to delete this deck");
    }

    @GetMapping("/getDeck")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getDeckById(Authentication authentication, @RequestParam int deckId)
    {
        DeckAccessServiceResponse response;
        try{
            response = resourceAccessService.getDeckAccessLevel(authentication,deckId);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        AccessLevel al = response.getAccessLevel();

        if(al != null)
        {
            return ResponseEntity.ok(response.getDeck());
        }
        return ResponseEntity.badRequest().body("You do not have permission to get this deck");
    }

    @GetMapping("/getDeckInfo")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getDeckInfoById(Authentication authentication, @RequestParam int deckId)
    {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }
        DeckAccessServiceResponse response;
        try{
            response = resourceAccessService.getDeckAccessLevel(authentication,deckId);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        AccessLevel al = response.getAccessLevel();

        if(al != null)
        {
            return ResponseEntity.ok(deckMapper.toDTO(customer, response.getDeck()));
        }
        return ResponseEntity.badRequest().body("You do not have permission to get this deck");
    }

}
