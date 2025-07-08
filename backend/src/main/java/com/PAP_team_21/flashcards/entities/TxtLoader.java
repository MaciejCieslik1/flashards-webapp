package com.PAP_team_21.flashcards.entities;

import com.PAP_team_21.flashcards.UserAnswer;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.deck.Deck;
import com.PAP_team_21.flashcards.services.DeckService;
import com.PAP_team_21.flashcards.entities.flashcard.Flashcard;
import com.PAP_team_21.flashcards.services.FlashcardService;
import com.PAP_team_21.flashcards.entities.flashcardProgress.FlashcardProgress;
import com.PAP_team_21.flashcards.entities.flashcardProgress.FlashcardProgressRepository;
import com.PAP_team_21.flashcards.entities.folder.Folder;
import com.PAP_team_21.flashcards.entities.reviewLog.ReviewLog;
import com.PAP_team_21.flashcards.entities.reviewLog.ReviewLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TxtLoader {
    private final FlashcardService flashcardService;
    private final DeckService deckService;
    private final ReviewLogRepository reviewLogRepository;
    private final FlashcardProgressRepository flashcardProgressRepository;

    public Deck loadDeckFromTxt(byte[] txtData, Folder folderParent, Customer customer) {
        String content = new String(txtData, StandardCharsets.UTF_8);
        String[] lines = content.split("\n");

        if (lines.length < 3 || !lines[0].startsWith("Deck: ")) {
            throw new IllegalArgumentException("Invalid TXT format");
        }

        String deckName = lines[0].substring(6).trim();
        Deck deck = new Deck(deckName, folderParent);
        deckService.save(deck);

        for (int i = 4; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\s{2,}"); // Split by 2+ spaces
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid line format: " + line);
            }

            String front = parts[0].trim();
            String back = parts[1].trim();
            Flashcard flashcard = new Flashcard(deck, front, back);
            flashcardService.save(flashcard);

            ReviewLog reviewLog = new ReviewLog(flashcard, customer, LocalDateTime.now(), UserAnswer.FORGOT);
            reviewLogRepository.save(reviewLog);
            FlashcardProgress flashcardProgress = new FlashcardProgress(flashcard, customer, LocalDateTime.now(),
                    reviewLog);
            flashcardProgressRepository.save(flashcardProgress);
        }

        return deck;
    }
}

