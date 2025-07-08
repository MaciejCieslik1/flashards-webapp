package com.PAP_team_21.flashcards.controllers.DTOMappers;

import com.PAP_team_21.flashcards.controllers.DTO.UserStatisticsDTO;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.services.FlashcardService;
import com.PAP_team_21.flashcards.entities.userStatistics.UserStatistics;
import com.PAP_team_21.flashcards.entities.userStatistics.UserStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserStatisticsMapper {

    private final FlashcardService flashcardService;
    private final UserStatisticsRepository userStatisticsRepository;

    public UserStatisticsDTO toDTO(Customer customer, UserStatistics userStatistics) {

        int customerId = customer.getId();

        int daysLearning = userStatistics.getTotalDaysLearning();
        int longestStreak = userStatistics.getLongestLearningStreak();
        int currentStreak = userStatistics.getDaysLearningStreak();
        int allNewCards = flashcardService.countAllNewCards(customerId) ;
        int allLearningCards = flashcardService.countAllDueCards(customerId);
        int allRememberedCards = flashcardService.countAllCards(customerId) - allLearningCards -allNewCards;
        List<LocalDate> loginDates = userStatisticsRepository.getGithubStyleChartData(customerId)
                .stream()
                .map(java.sql.Date::toLocalDate)
                .collect(Collectors.toList());

        return new UserStatisticsDTO(daysLearning, longestStreak, currentStreak, allNewCards, allLearningCards, allRememberedCards, loginDates);
    }
}

