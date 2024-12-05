package com.PAP_team_21.flashcards.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name="Flashcards_Progresses")
@Getter
@Setter
public class FlashcardProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @Column(name="flashcard_id")
    private int flashcard_id;

    @Column(name="user_id")
    private int user_id;

    @Column(name="next_review")
    private LocalDateTime next_review;

    @Column(name="valid")
    private boolean valid;

    public FlashcardProgress() {}

    public FlashcardProgress(int flashcard_id, int user_id, LocalDateTime next_review, boolean valid) {
        this.flashcard_id = flashcard_id;
        this.user_id = user_id;
        this.next_review = next_review;
        this.valid = valid;
    }
}
