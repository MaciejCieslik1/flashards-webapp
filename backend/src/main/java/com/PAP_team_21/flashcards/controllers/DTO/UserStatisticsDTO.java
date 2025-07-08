package com.PAP_team_21.flashcards.controllers.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserStatisticsDTO
{
    private int daysLearning;
    private int longestStreak;
    private int currentStreak;
    private int allNewCards;
    private int allLearningCards;
    private int allRememberedCards;
    private List<LocalDate> loginDates;
}
