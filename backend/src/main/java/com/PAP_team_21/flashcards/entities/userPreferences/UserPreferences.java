package com.PAP_team_21.flashcards.entities.userPreferences;

import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "User_Preferences")
@Getter
@Setter
public class UserPreferences {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonView(JsonViewConfig.Public.class)
    private int id;

    @Column(name = "user_id")
    @JsonView(JsonViewConfig.Public.class)
    private int userId;

    @Column(name = "dark_mode")
    @JsonView(JsonViewConfig.Public.class)
    private boolean darkMode;

    @Column(name = "language")
    @JsonView(JsonViewConfig.Public.class)
    private int language;

    @Column(name = "reminder_time")
    @JsonView(JsonViewConfig.Public.class)
    private LocalTime reminderTime;

    @Column(name = "timezone")
    @JsonView(JsonViewConfig.Public.class)
    private int timezone;

    @Column(name = "study_reminders")
    @JsonView(JsonViewConfig.Public.class)
    private int studyReminders;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private Customer customer;

    public UserPreferences() {}

    public UserPreferences(int userId, boolean darkMode, int language) {
        this.userId = userId;
        this.darkMode = darkMode;
        this.language = language;
        this.reminderTime = LocalTime.of(16, 30);
        this.timezone = 1;
        this.studyReminders = 1;
    }
}
