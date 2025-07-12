package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.entities.CustomerWithAvatar;
import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.services.FriendshipService;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/friendship")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @GetMapping("/getFriendship/{id}")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getFriendships(Authentication authentication, @PathVariable int id) {
        try {
            CustomerWithAvatar customerWithAvatar = friendshipService.getFriendshipById(authentication, id);
            return ResponseEntity.ok(customerWithAvatar);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFriendship(Authentication authentication, @RequestParam int friendshipId) {
        try {
            String communicate = friendshipService.deleteFriendship(authentication, friendshipId);
            return ResponseEntity.ok(communicate);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
