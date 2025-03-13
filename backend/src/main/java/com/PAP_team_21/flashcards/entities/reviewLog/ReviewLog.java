package com.PAP_team_21.flashcards.entities.reviewLog;


import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.UserAnswer;

import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.flashcard.Flashcard;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "Review_Logs")
@Getter
@Setter
public class ReviewLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonView(JsonViewConfig.Public.class)
    private int id;

    @Column(name = "flashcard_id", insertable = false, updatable = false)
    @JsonView(JsonViewConfig.Public.class)
    private int flashcardId;

    @Column(name = "user_id", insertable = false, updatable = false)
    @JsonView(JsonViewConfig.Public.class)
    private int userId;


    @Column(name = "`when`")
    @JsonView(JsonViewConfig.Public.class)
    private LocalDateTime when;

    @Column(name = "user_answer")
    @JsonView(JsonViewConfig.Public.class)
    private UserAnswer userAnswer;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "flashcard_id")
    private Flashcard flashcard;

    public ReviewLog() {}

    public ReviewLog(Flashcard flashcard, Customer customer, LocalDateTime when, UserAnswer userAnswer) {
        this.flashcard = flashcard;
        this.customer = customer;
        this.when = when;
        this.userAnswer = userAnswer;
    }

    public ReviewLog(Flashcard flashcard, Customer customer, UserAnswer userAnswer) {
        this.flashcard = flashcard;
        this.customer = customer;
        this.when = LocalDateTime.now();
        this.userAnswer = userAnswer;
    }


    public ReviewLog(int flashcardId, int userId, UserAnswer userAnswer) {
        this.flashcardId = flashcardId;
        this.userId = userId;
        this.when = LocalDateTime.now();
        this.userAnswer = userAnswer;
    }
}
