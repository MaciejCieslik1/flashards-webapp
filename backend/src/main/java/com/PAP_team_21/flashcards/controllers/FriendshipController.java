package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.entities.CustomerWithAvatar;
import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerRepository;
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

    @GetMapping("/getFriendship/{id}")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getFriendships(Authentication authentication, @PathVariable int id) {
        String email = authentication.getName();
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);
        if(customerOpt.isEmpty())
        {
            return ResponseEntity.badRequest().body("No user with this id found");
        }
        Customer customer = customerOpt.get();

        Optional<Friendship> friendshipOpt = friendshipRepository.findById(id);
        if (friendshipOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("No friendship with this id found");
        }
        Friendship friendship = friendshipOpt.get();

        Optional<Customer> friendOpt;
        if (friendship.getReceiverId() == customer.getId()) {
            friendOpt = customerRepository.findById(friendship.getSenderId());
        }

        else {
            friendOpt = customerRepository.findById(friendship.getReceiverId());
        }
        return getFriendWithAvatarResponseEntity(friendOpt);
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
        String email = authentication.getName();
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);
        if(customerOpt.isEmpty())
        {
            return ResponseEntity.badRequest().body("No user with this id found");
        }
        Customer customer = customerOpt.get();

        Optional<Friendship> friendshipOpt = friendshipRepository.findById(friendshipId);
        if (friendshipOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("No friendship with this id found");
        }
        Friendship friendship = friendshipOpt.get();

        if (friendship.getReceiverId() == customer.getId() || friendship.getSenderId() == customer.getId()) {
            friendshipRepository.delete(friendship);
            return ResponseEntity.ok("Friendship successfully deleted");
        }

        return ResponseEntity.badRequest().body("User do not have access to this friendship");
    }

}
