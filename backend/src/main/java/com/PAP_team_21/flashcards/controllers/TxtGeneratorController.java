package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.AccessLevel;
import com.PAP_team_21.flashcards.entities.TxtGenerator;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerRepository;
import com.PAP_team_21.flashcards.entities.deck.Deck;
import com.PAP_team_21.flashcards.services.DeckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class TxtGeneratorController {
    private final DeckService deckService;
    private final CustomerRepository customerRepository;
    private final TxtGenerator txtGenerator;

    @GetMapping("/generateTxt/{id}")
    public ResponseEntity<byte[]> generateTxt(Authentication authentication, @PathVariable int id) {
        String email = authentication.getName();
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);

        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                    .body("No user with this id found".getBytes(StandardCharsets.UTF_8));
        }

        Customer customer = customerOpt.get();
        Optional<Deck> deckOpt = deckService.findById(id);

        if (deckOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                    .body("Deck not found".getBytes(StandardCharsets.UTF_8));
        }

        Deck deck = deckOpt.get();
        AccessLevel al = deck.getAccessLevel(customer);

        if (al == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                    .body("You don't have access to this deck".getBytes(StandardCharsets.UTF_8));
        }

        byte[] txtBytes = txtGenerator.generateTxtFromDeck(deck);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + deck.getName() + ".txt");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8");

        return new ResponseEntity<>(txtBytes, headers, HttpStatus.OK);
    }
}
