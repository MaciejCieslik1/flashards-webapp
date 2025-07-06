package com.PAP_team_21.flashcards.entities.friendship;

import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.notification.Notification;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "Friendships")
@Getter
@Setter
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonView(JsonViewConfig.Public.class)
    private int id;

    @Column(name = "sender_id")
    @JsonView(JsonViewConfig.Public.class)
    private int senderId;

    @Column(name = "receiver_id")
    @JsonView(JsonViewConfig.Public.class)
    private int receiverId;

    @Column(name = "accepted")
    @JsonView(JsonViewConfig.Public.class)
    private boolean accepted;

    @ManyToOne
    @JoinColumn(name = "sender_id", insertable = false, updatable = false)
    @JsonIgnore
    private Customer sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", insertable = false, updatable = false)
    @JsonIgnore
    private Customer receiver;

    @ManyToMany
    @JoinTable (
            name = "Friendships_Notifications",
            joinColumns = @JoinColumn(name = "friendship_id"),
            inverseJoinColumns = @JoinColumn(name = "notification_id")
    )
    @JsonIgnore
    private List<Notification> notifications;

    public Friendship() {}

    public Friendship(int senderId, int receiverId, boolean accepted) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.accepted = accepted;
    }
}
