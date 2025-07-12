package com.PAP_team_21.flashcards.services;

import com.PAP_team_21.flashcards.AccessLevel;
import com.PAP_team_21.flashcards.Errors.NoPermissionException;
import com.PAP_team_21.flashcards.Errors.ResourceNotFoundException;
import com.PAP_team_21.flashcards.authentication.ResourceAccessLevelService.DeckAccessServiceResponse;
import com.PAP_team_21.flashcards.authentication.ResourceAccessLevelService.FolderAccessServiceResponse;
import com.PAP_team_21.flashcards.authentication.ResourceAccessLevelService.ResourceAccessService;
import com.PAP_team_21.flashcards.controllers.DTO.DeckDTO;
import com.PAP_team_21.flashcards.controllers.DTOMappers.DeckMapper;
import com.PAP_team_21.flashcards.controllers.requests.DeckCreationRequest;
import com.PAP_team_21.flashcards.controllers.requests.DeckUpdateRequest;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.deck.Deck;
import com.PAP_team_21.flashcards.entities.deck.DeckRepository;
import com.PAP_team_21.flashcards.entities.flashcard.Flashcard;
import com.PAP_team_21.flashcards.entities.folder.Folder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class DeckService {
    private final DeckRepository deckRepository;
    private final CustomerService customerService;
    private final ResourceAccessService resourceAccessService;

    @Transactional
    public List<Deck> getLastUsedDecks(int userId, int howMany) {
        return deckRepository.getLastUsed(userId, howMany);
    }

    @Transactional(readOnly = true)
    public Optional<Deck> findById(int deckId) {
        return deckRepository.findById(deckId);
    }

    @Transactional
    public Deck save(Deck deck) {
        return deckRepository.save(deck);
    }

    @Transactional
    public void delete(Deck deck) {
        deckRepository.delete(deck);
    }

    public int countDeckTotalCards(Integer customerId, int deckId) {
        return deckRepository.countDeckAllCards(customerId, deckId);
    }

    public int countDeckNewCards(Integer customerId, int deckId) {
        return deckRepository.countDeckNewCards(customerId, deckId);
    }

    public int countDeckAllDueCards(Integer customerId, int deckId) {
        return deckRepository.countDeckAllDueCards(customerId, deckId);
    }

    public List<Flashcard> getFlashcards(Authentication authentication, int deckId, int page, int size,
                                         String sortBy, boolean ascending) {
        DeckAccessServiceResponse response;

        response = resourceAccessService.getDeckAccessLevel(authentication, deckId, this);
        
        AccessLevel al = response.getAccessLevel();
        if (al==null) {
            throw new ResourceNotFoundException("You dont have access to this deck");
        }
        return response.getDeck().getFlashcards(page, size, sortBy, ascending);
    }

    @Transactional
    public List<DeckDTO> getLastUsed(Authentication authentication, int howMany) {
        if(howMany <= 0) {
            throw new IllegalArgumentException("howMany must be greater than 0");
        }

        Customer customer;
        customer = customerService.checkForLoggedCustomer(authentication);

        List<Deck> decks = this.getLastUsedDecks(customer.getId(), howMany);
        DeckMapper deckMapper = new DeckMapper(this);
        return deckMapper.toDTO(customer, decks);
    }

    public List<Deck> getAllDecks(Authentication authentication, FolderService folderService) {
        Customer customer;
        customer = customerService.checkForLoggedCustomer(authentication);
        return findDecks(customer, folderService);
    }

    private List<Deck> findDecks(Customer customer, FolderService folderService) {
        List<Deck> result = new ArrayList<>();
        List<Folder> folders = folderService.findAllUserFolders(customer.getId());

        for(Folder f: folders) {
            result.addAll(f.getDecks());
        }
        return result;
    }

    public List<DeckDTO> getAllDecksInfo(Authentication authentication, FolderService folderService) {
        DeckMapper deckMapper = new DeckMapper(this);
        Customer customer;
        customer = customerService.checkForLoggedCustomer(authentication);
        List<Deck> result = findDecks(customer, folderService);
        return deckMapper.toDTO(customer, result);
    }

    @Transactional
    public DeckDTO createDeck(Authentication authentication, DeckCreationRequest request) {
        FolderAccessServiceResponse response;
        DeckMapper deckMapper = new DeckMapper(this);
        response = resourceAccessService.getFolderAccessLevel(authentication, request.getFolderId());
        AccessLevel al = response.getAccessLevel();
        if(al != null && (al.equals(AccessLevel.OWNER) || al.equals(AccessLevel.EDITOR))) {
            Deck deck = new Deck(request.getName(), response.getFolder());
            this.save(deck);
            return deckMapper.toDTO(response.getCustomer(), deck);
        }
        else {
            throw new NoPermissionException("You do not have permission to create a deck here");
        }
    }

    @Transactional
    public DeckDTO updateDeck(Authentication authentication, DeckUpdateRequest request) {
        DeckAccessServiceResponse response;
        DeckMapper deckMapper = new DeckMapper(this);

        response = resourceAccessService.getDeckAccessLevel(authentication, request.getDeckId(),
                this);
        AccessLevel al = response.getAccessLevel();

        if (al != null && (al.equals(AccessLevel.OWNER) || al.equals(AccessLevel.EDITOR))) {
            response.getDeck().setName(request.getName());
            this.save(response.getDeck());
            return deckMapper.toDTO(response.getCustomer(), response.getDeck());
        }
        else {
            throw new NoPermissionException("You do not have permission to update deck");
        }
    }

    @Transactional
    public String deleteDeck(Authentication authentication, int deckId) {
        DeckAccessServiceResponse response;
        response = resourceAccessService.getDeckAccessLevel(authentication, deckId, this);
        AccessLevel al = response.getAccessLevel();

        if(al != null && (al.equals(AccessLevel.OWNER) || al.equals(AccessLevel.EDITOR))) {
            this.delete(response.getDeck());
            return "deck deleted";
        }
        else {
            throw new NoPermissionException("You do not have permission to delete this deck");
        }
    }

    public Deck getDeck(Authentication authentication, int deckId) {
        DeckAccessServiceResponse response;
        response = resourceAccessService.getDeckAccessLevel(authentication, deckId, this);
        AccessLevel al = response.getAccessLevel();

        if(al != null) {
            return response.getDeck();
        }
        else {
            throw new NoPermissionException("You do not have permission to get this deck");
        }
    }

    public DeckDTO getDeckInfo(Authentication authentication, int deckId) {
        DeckMapper deckMapper = new DeckMapper(this);
        Customer customer;
        customer = customerService.checkForLoggedCustomer(authentication);
        DeckAccessServiceResponse response;
        response = resourceAccessService.getDeckAccessLevel(authentication, deckId, this);
        AccessLevel al = response.getAccessLevel();

        if(al != null) {
            return deckMapper.toDTO(customer, response.getDeck());
        }
        else {
            throw new NoPermissionException("You do not have permission to get this deck");
        }
    }
}
