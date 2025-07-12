package com.PAP_team_21.flashcards.services;

import com.PAP_team_21.flashcards.AccessLevel;
import com.PAP_team_21.flashcards.Errors.EmptyFileException;
import com.PAP_team_21.flashcards.Errors.InvalidFileFormatException;
import com.PAP_team_21.flashcards.entities.TxtLoader;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.deck.Deck;
import com.PAP_team_21.flashcards.entities.folder.Folder;
import com.PAP_team_21.flashcards.entities.folder.FolderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TxtLoaderService {
    private final CustomerService customerService;
    private final DeckService deckService;
    private final FolderJpaRepository folderJpaRepository;
    private final TxtLoader txtLoader;

    public Deck loadDeckTxt(Authentication authentication, int folderId, MultipartFile file)
            throws IOException {
        Customer customer = customerService.checkForLoggedCustomer(authentication);

        if (file.isEmpty()) {
            throw new EmptyFileException("File cannot be empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".txt")) {
            throw new InvalidFileFormatException("Only .txt files are supported");
        }

        if (folderId < 0) {
            throw new IllegalArgumentException("Invalid folder id");
        }

        Optional<Folder> folderOpt = folderJpaRepository.findById(folderId);
        if (folderOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid folder id");
        }

        Folder folder = folderOpt.get();
        AccessLevel al = folder.getAccessLevel(customer);
        if (al == null) {
            throw new IllegalArgumentException("You don't have access to this folder");
        }

        byte[] txtData = file.getBytes();
        Deck deck = txtLoader.loadDeckFromTxt(txtData, folder, customer);
        deckService.save(deck);
        return deck;
    }
}
