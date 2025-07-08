package com.PAP_team_21.flashcards.authentication.ResourceAccessLevelService;

import com.PAP_team_21.flashcards.Errors.ResourceNotFoundException;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerRepository;
import com.PAP_team_21.flashcards.entities.deck.Deck;
import com.PAP_team_21.flashcards.services.DeckService;
import com.PAP_team_21.flashcards.entities.flashcard.Flashcard;
import com.PAP_team_21.flashcards.services.FlashcardService;
import com.PAP_team_21.flashcards.entities.folder.Folder;
import com.PAP_team_21.flashcards.entities.folder.FolderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResourceAccessService {
    private final CustomerRepository customerRepository;
    private final DeckService deckService;
    private final FlashcardService flashcardService;
    private final FolderJpaRepository folderJpaRepository;

    private Customer getCustomer(Authentication authentication) throws ResourceNotFoundException
    {
        String email = authentication.getName();

        Optional<Customer> customer = customerRepository.findByEmail(email);
        if(customer.isEmpty())
            throw new ResourceNotFoundException("User not found");
        return customer.get();
    }

    public FolderAccessServiceResponse getFolderAccessLevel(Authentication authentication, int folderId) throws ResourceNotFoundException
    {
        Customer customer = getCustomer(authentication);

        Optional<Folder> folder = folderJpaRepository.findById(folderId);
        if(folder.isEmpty())
            throw new ResourceNotFoundException("Folder not found");

        return new FolderAccessServiceResponse(folder.get(), folder.get().getAccessLevel(customer), customer);
    }

    public DeckAccessServiceResponse getDeckAccessLevel(Authentication authentication, int deckId) throws ResourceNotFoundException
    {
        Customer customer = getCustomer(authentication);

        Optional<Deck> deck =  deckService.findById(deckId);
        if(deck.isEmpty())
            throw new ResourceNotFoundException("Deck not found");

        return new DeckAccessServiceResponse(deck.get(), deck.get().getAccessLevel(customer), customer);
    }

    public FlashcardAccessServiceResponse getFlashcardAccessLevel(Authentication authentication, int flashcardId)
    {
        Customer customer = getCustomer(authentication);

        Optional<Flashcard> flashcard = flashcardService.findById(flashcardId);

        if(flashcard.isEmpty())
            throw new ResourceNotFoundException("Flashcard not found");

        return new FlashcardAccessServiceResponse(flashcard.get(), flashcard.get().getAccessLevel(customer), customer);
    }
}
