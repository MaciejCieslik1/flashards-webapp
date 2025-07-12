package com.PAP_team_21.flashcards.services;

import com.PAP_team_21.flashcards.controllers.DTO.UserStatisticsDTO;
import com.PAP_team_21.flashcards.controllers.DTOMappers.UserStatisticsMapper;
import com.PAP_team_21.flashcards.controllers.requests.UserStatisticsUpdateRequest;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerRepository;
import com.PAP_team_21.flashcards.entities.userStatistics.UserStatistics;
import com.PAP_team_21.flashcards.entities.userStatistics.UserStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserStatisticsService {
    private final CustomerService customerService;
    private final UserStatisticsMapper userStatisticsMapper;
    private final CustomerRepository customerRepository;
    private final UserStatisticsRepository userStatisticsRepository;

    public UserStatisticsDTO getUserStatistics(Authentication authentication) {
        Customer customer = customerService.checkForLoggedCustomer(authentication);
        UserStatistics userStatistics = customer.getUserStatistics();

        return userStatisticsMapper.toDTO(customer, userStatistics);
    }

    public UserStatistics updateUserStatistics(Authentication authentication, UserStatisticsUpdateRequest
            request) {
        String email = authentication.getName();
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);
        if (customerOpt.isEmpty()) {
            throw new IllegalArgumentException("No user with this id found");
        }

        Customer customer = customerOpt.get();
        UserStatistics userStatistics = customer.getUserStatistics();
        userStatistics.setTotalTimeSpent(request.getTotalTimeSpent());
        userStatistics.setLoginCount(request.getLoginCount());
        userStatistics.setLastLogin(request.getLastLogin());
        userStatisticsRepository.save(userStatistics);

        return userStatistics;
    }
}
