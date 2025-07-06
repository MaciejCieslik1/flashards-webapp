package com.PAP_team_21.flashcards.entities.userStatistics;

import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Entity
@Table(name="User_Statistics")
@Getter
@Setter
public class UserStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonView(JsonViewConfig.Public.class)
    private int id;

    @Column(name = "user_id")
    @JsonView(JsonViewConfig.Public.class)
    private int userId;

    @Column(name = "total_time_spent")
    @JsonView(JsonViewConfig.Public.class)
    private int totalTimeSpent;

    @Column(name = "login_count")
    @JsonView(JsonViewConfig.Public.class)
    private int loginCount;

    @Column(name = "last_login")
    @JsonView(JsonViewConfig.Public.class)
    private LocalDateTime lastLogin;

    @Column(name = "total_days_learning")
    @JsonView(JsonViewConfig.Public.class)
    private int totalDaysLearning;

    @Column(name = "days_learning_streak")
    @JsonView(JsonViewConfig.Public.class)
    private int daysLearningStreak;

    @Column(name = "longest_learning_streak")
    @JsonView(JsonViewConfig.Public.class)
    private int longestLearningStreak;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private Customer customer;

    public UserStatistics() {}

    public UserStatistics(int userId, int totalTimeSpent, int loginCount, LocalDateTime lastLogin) {
        this.userId = userId;
        this.totalTimeSpent = totalTimeSpent;
        this.loginCount = loginCount;
        this.lastLogin = lastLogin;
    }

    public void updateStatistics()
    {
        daysLearningStreak++;
        longestLearningStreak++;
        totalDaysLearning++;
    }

    public void updateStatisticsFirstDay()
    {
        daysLearningStreak = 1;
        longestLearningStreak = 1;
        totalDaysLearning = 1;
    }

    public void updateStatisticsCancelStreak()
    {
        daysLearningStreak = 1;
        totalDaysLearning ++;
    }


    public void updateStreak(List<LocalDate> loginDates) {
        if (loginDates.size() == 1) {
            updateStatisticsFirstDay();
        }

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        HashMap<LocalDate, Integer> dateOccurrences = new HashMap<>();
        for (LocalDate loginDate : loginDates) {
            if (dateOccurrences.containsKey(loginDate)) {
                dateOccurrences.put(loginDate, dateOccurrences.get(loginDate) + 1);
            }
            else {
                dateOccurrences.put(loginDate, 1);
            }
        }

        int todayCount = dateOccurrences.getOrDefault(today, 0);
        int yesterdayCount = dateOccurrences.getOrDefault(yesterday, 0);

        if (todayCount == 1 && yesterdayCount >= 1) {
            updateStatistics();
        }
        else if (todayCount == 1 && yesterdayCount == 0 && totalDaysLearning == 0) {
            updateStatisticsCancelStreak();
        }

        else if (todayCount == 1 && yesterdayCount == 0) {
            updateStatisticsFirstDay();
        }
    }
}
