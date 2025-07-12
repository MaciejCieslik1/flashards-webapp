package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.entities.deck.Deck;
import com.PAP_team_21.flashcards.services.TxtLoaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class TxtLoaderController {
    private final TxtLoaderService txtLoaderService;

    @PostMapping("/loadDeckTxt")
    public ResponseEntity<?> loadDeckTxt(Authentication authentication,
                                         @RequestParam("fileToLoad") MultipartFile file,
                                         @RequestParam("folderId") int folderId) {
        try {
            Deck deck = txtLoaderService.loadDeckTxt(authentication, folderId, file);
            return ResponseEntity.ok(deck);
        }
        catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid TXT format:" + e);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
