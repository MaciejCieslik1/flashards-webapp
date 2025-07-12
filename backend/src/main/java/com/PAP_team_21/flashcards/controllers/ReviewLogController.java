package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.reviewLog.ReviewLog;
import com.PAP_team_21.flashcards.services.ReviewLogService;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/reviewLog")
@RequiredArgsConstructor
public class ReviewLogController {
    private final ReviewLogService reviewLogService;

    @GetMapping("/getReviewLog/{id}")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getReviewLog(Authentication authentication, @PathVariable int id) {
        try {
            ReviewLog reviewLog = reviewLogService.getReviewLogById(authentication, id);
            return ResponseEntity.ok(reviewLog);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getAllReviewLogs")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getAllReviewLogs(Authentication authentication) {
        try {
            List<ReviewLog> reviewLogs = reviewLogService.getAllReviewLogs(authentication);
            return ResponseEntity.ok(reviewLogs);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteReviewLog(Authentication authentication,
                                                @RequestParam int reviewLogId) {
        try {
            String communicate = reviewLogService.deleteReviewLogById(authentication, reviewLogId);
            return ResponseEntity.ok(communicate);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
