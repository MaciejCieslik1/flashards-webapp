package com.PAP_team_21.flashcards.entities.notification;

import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.friendship.Friendship;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Notifications")
@Getter
@Setter
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonView(JsonViewConfig.Public.class)
    private int id;

    @Column(name = "user_id")
    @JsonView(JsonViewConfig.Public.class)
    private int userId;

    @Column(name = "received")
    @JsonView(JsonViewConfig.Public.class)
    private boolean received;

    @Column(name = "text")
    @JsonView(JsonViewConfig.Public.class)
    private String text;

    @Column(name = "creation_date")
    @JsonView(JsonViewConfig.Internal.class)
    private LocalDateTime creationDate;

    @Column(name = "received_date")
    @JsonView(JsonViewConfig.Public.class)
    private LocalDateTime receivedDate;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonView(JsonViewConfig.Internal.class)
    private Customer customer;

    @ManyToMany(mappedBy = "notifications", fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE,
                        CascadeType.DETACH, CascadeType.REFRESH})
    private List<Friendship> friendships;

    public Notification() {}

    public Notification(int userId, boolean received, String text) {
        this.userId = userId;
        this.received = received;
        this.text = text;
        this.creationDate = LocalDateTime.now();
        this.receivedDate = LocalDateTime.now();
    }

    public Notification(int userId, String text) {
        this.userId = userId;
        this.received = true;
        this.text = text;
        this.creationDate = LocalDateTime.now();
        this.receivedDate = LocalDateTime.now();
    }
}
