package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.AccessLevel;
import com.PAP_team_21.flashcards.entities.TxtLoader;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerService;
import com.PAP_team_21.flashcards.entities.deck.Deck;
import com.PAP_team_21.flashcards.entities.deck.DeckService;
import com.PAP_team_21.flashcards.entities.folder.Folder;
import com.PAP_team_21.flashcards.entities.folder.FolderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class TxtLoaderController {
    private final TxtLoader txtLoader;
    private final DeckService deckService;
    private final FolderJpaRepository folderJpaRepository;
    private final CustomerService customerService;

    @PostMapping("/loadDeckTxt")
    public ResponseEntity<?> loadDeckTxt(Authentication authentication,
                                         @RequestParam("fileToLoad") MultipartFile file,
                                         @RequestParam("folderId") int folderId) {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File cannot be empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".txt")) {
            return ResponseEntity.badRequest().body("Only .txt files are supported");
        }

        if (folderId < 0) {
            return ResponseEntity.badRequest().body("Invalid folder id");
        }

        Optional<Folder> folderOpt = folderJpaRepository.findById(folderId);
        if (folderOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid folder id");
        }
        Folder folder = folderOpt.get();

        AccessLevel al = folder.getAccessLevel(customer);
        if (al == null)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body("You don't have access to this folder");
        }

        try {
            byte[] txtData = file.getBytes();
            Deck deck = txtLoader.loadDeckFromTxt(txtData, folder, customer);
            deckService.save(deck);
            return ResponseEntity.ok(deck);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid TXT format:" + e);
        }
    }
}
