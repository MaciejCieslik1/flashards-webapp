package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.controllers.requests.NotificationCreationByEmailRequest;
import com.PAP_team_21.flashcards.controllers.requests.NotificationCreationRequest;
import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.notification.Notification;
import com.PAP_team_21.flashcards.services.NotificationService;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/getNotification/{id}")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getNotification(Authentication authentication, @PathVariable int id) {
        try {
            Notification notification = notificationService.getNotificationById(authentication, id);
            return ResponseEntity.ok(notification);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createNotification(Authentication authentication,
                                                @RequestBody NotificationCreationRequest request) {
        try {
            Notification notification = notificationService.createNotification(authentication, request);
            return ResponseEntity.ok(notification);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/createByEmail")
    public ResponseEntity<?> createNotification(Authentication authentication,
                                                @RequestBody NotificationCreationByEmailRequest request) {
        try {
            Notification notification = notificationService.createNotificationByEmail(authentication, request);
            return ResponseEntity.ok(notification);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteNotification(Authentication authentication,
                                                @RequestParam int notificationId) {
        try {
            String communicate = notificationService.deleteNotification(authentication, notificationId);
            return ResponseEntity.ok(communicate);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
