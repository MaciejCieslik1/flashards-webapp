package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.entities.CustomerWithAvatar;
import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerRepository;
import com.PAP_team_21.flashcards.entities.customer.CustomerService;
import com.PAP_team_21.flashcards.entities.friendship.Friendship;
import com.PAP_team_21.flashcards.entities.friendship.FriendshipRepository;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/friendship")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipRepository friendshipRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;

    @GetMapping("/getFriendship/{id}")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getFriendships(Authentication authentication, @PathVariable int id) {
        Optional<Customer> friendOpt;
        try {
            friendOpt = manageAuthenticationAndFindFriendOpt(authentication, id);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e);
        }
        return getFriendWithAvatarResponseEntity(friendOpt);
    }

    private Optional<Customer> manageAuthenticationAndFindFriendOpt(Authentication authentication, int id) {
        Customer customer = customerService.checkForLoggedCustomer(authentication);
        Friendship friendship;
        friendship = getFriendship(id);
        Optional<Customer> friendOpt;
        if (friendship.getReceiverId() == customer.getId()) {
            friendOpt = customerRepository.findById(friendship.getSenderId());
        }
        else {
            friendOpt = customerRepository.findById(friendship.getReceiverId());
        }
        return friendOpt;
    }

    private Friendship getFriendship(int id) {
        return friendshipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Friendship with id " + id + " not found"));
    }

    public ResponseEntity<?> getFriendWithAvatarResponseEntity(Optional<Customer> friendOpt) {
        if (friendOpt.isPresent()) {
            Customer friend = friendOpt.get();
            byte[] avatar = friend.getAvatar();
            if (avatar == null) {
                return ResponseEntity.badRequest().body("Error fetching avatar");
            }
            CustomerWithAvatar friendWithAvatar = new CustomerWithAvatar(friend, avatar);
            return ResponseEntity.ok(friendWithAvatar);
        }
        else {
            return ResponseEntity.badRequest().body("No customer with this id found");
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFriendship(Authentication authentication, @RequestParam int friendshipId) {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        Friendship friendship;
        try {
            friendship = getFriendship(friendshipId);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No friendship with this id found");
        }

        if (friendship.getReceiverId() == customer.getId() || friendship.getSenderId() == customer.getId()) {
            friendshipRepository.delete(friendship);
            return ResponseEntity.ok("Friendship successfully deleted");
        }

        return ResponseEntity.badRequest().body("User do not have access to this friendship");
    }
}
