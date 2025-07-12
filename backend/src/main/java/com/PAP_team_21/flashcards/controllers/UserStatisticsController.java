package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.controllers.DTO.UserStatisticsDTO;
import com.PAP_team_21.flashcards.controllers.requests.UserStatisticsUpdateRequest;
import com.PAP_team_21.flashcards.entities.userStatistics.UserStatistics;
import com.PAP_team_21.flashcards.services.UserStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/userStatistics")
@RequiredArgsConstructor
public class UserStatisticsController {
    private final UserStatisticsService userStatisticsService;

    @GetMapping("/getUserStatistics")
    @Transactional
    public ResponseEntity<?> getUserStatistics(Authentication authentication) {
        try {
            UserStatisticsDTO userStatisticsDTO = userStatisticsService.getUserStatistics(authentication);
            return ResponseEntity.ok(userStatisticsDTO);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateUserStatistics(Authentication authentication,
                                                   @RequestBody UserStatisticsUpdateRequest request) {
        try {
            UserStatistics userStatistics = userStatisticsService.updateUserStatistics(authentication,
                    request);
            return ResponseEntity.ok(userStatistics);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
