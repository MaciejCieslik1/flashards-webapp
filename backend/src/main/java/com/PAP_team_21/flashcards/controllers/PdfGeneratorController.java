package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.AccessLevel;
import com.PAP_team_21.flashcards.entities.PdfGenerator;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerService;
import com.PAP_team_21.flashcards.entities.deck.Deck;
import com.PAP_team_21.flashcards.entities.deck.DeckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class PdfGeneratorController {
    private final DeckService deckService;
    private final CustomerService customerService;
    private final PdfGenerator pdfGenerator;

    @GetMapping("/generatePdf/{id}")
    public ResponseEntity<byte[]> generatePdf(Authentication authentication, @PathVariable int id) {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                    .body("No user with this id found".getBytes(StandardCharsets.UTF_8));
        }


        Optional<Deck> deckOpt = deckService.findById(id);
        if (deckOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                    .body("Deck not found".getBytes(StandardCharsets.UTF_8));
        }
        Deck deck = deckOpt.get();

        AccessLevel al = deck.getAccessLevel(customer);
        if (al == null)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                    .body("You don't have access to this deck".getBytes(StandardCharsets.UTF_8));
        }

        byte[] pdfBytes = pdfGenerator.generatePdfFromDeck(deck);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + deck.getName() + ".pdf");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}