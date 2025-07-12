package com.PAP_team_21.flashcards.services;

import com.PAP_team_21.flashcards.AccessLevel;
import com.PAP_team_21.flashcards.Errors.DeckNotFoundException;
import com.PAP_team_21.flashcards.Errors.NoPermissionException;
import com.PAP_team_21.flashcards.entities.PdfGenerator;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.deck.Deck;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PdfGeneratorService {

    private final CustomerService customerService;
    private final DeckService deckService;
    private final PdfGenerator pdfGenerator;

    public byte[] generatePdf(Authentication authentication, int id) {
        Customer customer = customerService.checkForLoggedCustomer(authentication);
        Deck deck = PdfGeneratorService.getDeck(customer, id, deckService);

        return pdfGenerator.generatePdfFromDeck(deck);
    }

    public static Deck getDeck(Customer customer, int id, DeckService deckService) {
        Optional<Deck> deckOpt = deckService.findById(id);

        if (deckOpt.isEmpty()) {
            throw new DeckNotFoundException("Deck not found");
        }

        Deck deck = deckOpt.get();
        AccessLevel al = deck.getAccessLevel(customer);

        if (al == null) {
            throw new NoPermissionException("You don't have access to this deck");
        }
        return deck;
    }

    public HttpHeaders generateHeaders(Authentication authentication, int id) {
        customerService.checkForLoggedCustomer(authentication);
        Optional<Deck> deckOpt = deckService.findById(id);
        if (deckOpt.isEmpty()) {
            throw new IllegalArgumentException("Deck not found");
        }
        Deck deck = deckOpt.get();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + deck.getName() +
                ".pdf");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");
        return headers;
    }
}
