package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.Errors.AvatarNotFoundException;
import com.PAP_team_21.flashcards.controllers.requests.UpdateBioRequest;
import com.PAP_team_21.flashcards.controllers.requests.UpdateUsernameRequest;
import com.PAP_team_21.flashcards.entities.CustomerWithAvatar;
import com.PAP_team_21.flashcards.entities.FriendshipResponse;
import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.services.CustomerService;
import com.PAP_team_21.flashcards.entities.friendship.Friendship;
import com.PAP_team_21.flashcards.entities.notification.Notification;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/findById/{id}")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getCustomerById(Authentication authentication, @PathVariable int id) {
        try {
            customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        Optional<Customer> customerOpt = customerService.findCustomerById(id);

        if (customerOpt.isPresent()) {
            return ResponseEntity.ok(customerOpt.get());
        }
        return ResponseEntity.badRequest().body("Customer not found");
    }

    @GetMapping("/findByEmail/{email}")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getCustomerByEmail(Authentication authentication, @PathVariable String email) {
        try {
            customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        Optional<Customer> customerOpt = customerService.findCustomerByEmail(email);

        if (customerOpt.isPresent()) {
            return ResponseEntity.ok(customerOpt.get());
        }
        return ResponseEntity.badRequest().body("Customer not found");
    }

    @PostMapping("/updateUsername")
    public ResponseEntity<?> updateUsername(Authentication authentication, @RequestBody UpdateUsernameRequest request) {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
            customerService.updateUsername(customer, request);
            return ResponseEntity.ok("Username updated successfully");
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e);
        }
    }

    @PostMapping("/updateBio")
    public ResponseEntity<?> updateBio(Authentication authentication, @RequestBody UpdateBioRequest request) {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
            customerService.updateBio(customer, request);
            return ResponseEntity.ok("Biography updated successfully");
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }
    }

    @PostMapping("/updateAvatar")
    public ResponseEntity<?> updateAvatar(Authentication authentication, @RequestParam("avatar") MultipartFile avatar) {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        try {
            customerService.updateAvatar(customer, avatar);
            return ResponseEntity.ok("Profile picture updated successfully");
        }
        catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload avatar:" + e.getMessage());
        }
    }


    @GetMapping("/findByUsername/{username}")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getCustomersByUsername(Authentication authentication, @PathVariable String username) {
        try {
            customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        List<Customer> customers = customerService.findByUsername(username);

        if (customers.isEmpty()) {
            return ResponseEntity.badRequest().body("Customer not found");
        }
        return ResponseEntity.ok(customers);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteCustomer(Authentication authentication) {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }
        customerService.deleteCustomer(customer);
        return ResponseEntity.ok("Customer deleted successfully");
    }

    @GetMapping("/getSelf")
    @JsonView(JsonViewConfig.Internal.class)
    public ResponseEntity<?> getSelf(Authentication authentication) {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        try {
            CustomerWithAvatar customerWithAvatar = customerService.getSelf(customer);
            return ResponseEntity.ok(customerWithAvatar);
        }
        catch (AvatarNotFoundException e){
            return ResponseEntity.badRequest().body("Error fetching avatar: " + e.getMessage());
        }
    }

    @GetMapping("/getReceivedFriendships")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getReceivedFriendships(Authentication authentication) {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        List<Friendship> waitingReceivedFriendships = customerService.getReceivedFriendships(customer);

        return ResponseEntity.ok(waitingReceivedFriendships);
    }

    @GetMapping("/getSentFriendships")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getSentFriendships(Authentication authentication) {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        List<Friendship> sentFriendships = customerService.getSentFriendships(customer);
        return ResponseEntity.ok(sentFriendships);
    }

    @GetMapping("/getNotifications")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getNotifications(Authentication authentication) {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        List<Notification> notifications = customerService.getNotifications(customer);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/getFriends")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getFriends(Authentication authentication) {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        List<Customer> friends = customerService.getFriends(customer);

        return ResponseEntity.ok(friends);
    }

    @GetMapping("/getFriendById/{id}")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getFriendById(Authentication authentication, @PathVariable int id) {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        try {
            CustomerWithAvatar customerWithAvatar = customerService.getFriendById(customer, id);
            return ResponseEntity.ok(customerWithAvatar);
        }
        catch (AvatarNotFoundException e) {
            return ResponseEntity.badRequest().body(e);
        }
    }

    @GetMapping("/getFriendByEmail/{email}")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getFriendByEmail(Authentication authentication, @PathVariable String email) {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        try {
            CustomerWithAvatar customerWithAvatar = customerService.getFriendByEmail(customer, email);
            return ResponseEntity.ok(customerWithAvatar);
        }
        catch (AvatarNotFoundException e) {
            return ResponseEntity.badRequest().body(e);
        }
    }

    @PostMapping("/sendFriendshipOfferById/{id}")
    public ResponseEntity<?> sendFriendshipOfferById(Authentication authentication, @PathVariable int id) {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        try {
            FriendshipResponse friendshipResponse = customerService.sendFriendshipOfferById(customer, id);
            return ResponseEntity.ok(friendshipResponse);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e);
        }
    }

    @PostMapping("/sendFriendshipOfferByEmail/{email}")
    public ResponseEntity<?> sendFriendshipOfferByEmail(Authentication authentication, @PathVariable String email) {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        try {
            FriendshipResponse friendshipResponse = customerService.sendFriendshipOfferByEmail(customer, email);
            return ResponseEntity.ok(friendshipResponse);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e);
        }
    }

    @PostMapping("/acceptFriendshipOfferById/{id}")
    public ResponseEntity<?> acceptFriendshipOfferById(Authentication authentication, @PathVariable int id) {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        try {
            Friendship friendship = customerService.acceptFriendshipOfferById(customer, id);
            return ResponseEntity.ok(friendship);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e);
        }
    }
}

