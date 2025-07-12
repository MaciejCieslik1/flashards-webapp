package com.PAP_team_21.flashcards.services;

import com.PAP_team_21.flashcards.entities.TxtGenerator;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerRepository;
import com.PAP_team_21.flashcards.entities.deck.Deck;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TxtGeneratorService {
    private final DeckService deckService;
    private final CustomerRepository customerRepository;
    private final TxtGenerator txtGenerator;

    public byte[] generateTxtBytes(Authentication authentication, int id) {
        String email = authentication.getName();
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);

        if (customerOpt.isEmpty()) {
            throw new IllegalArgumentException("No user with this id found");
        }

        Customer customer = customerOpt.get();
        Deck deck = PdfGeneratorService.getDeck(customer, id, deckService);
        return txtGenerator.generateTxtFromDeck(deck);
    }

    public HttpHeaders generateTxtHeaders(Authentication authentication, int id) {
        String email = authentication.getName();
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);

        if (customerOpt.isEmpty()) {
            throw new IllegalArgumentException("No user with this id found");
        }
        Optional<Deck> deckOpt = deckService.findById(id);

        if (deckOpt.isEmpty()) {
            throw new IllegalArgumentException("Deck not found");
        }

        Deck deck = deckOpt.get();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" +
                deck.getName() + ".txt");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8");
        return headers;
    }
}
