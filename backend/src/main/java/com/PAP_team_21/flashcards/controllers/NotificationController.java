package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.controllers.requests.NotificationCreationByEmailRequest;
import com.PAP_team_21.flashcards.controllers.requests.NotificationCreationRequest;
import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerRepository;
import com.PAP_team_21.flashcards.services.CustomerService;
import com.PAP_team_21.flashcards.entities.notification.Notification;
import com.PAP_team_21.flashcards.entities.notification.NotificationRepository;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;

    @GetMapping("/getNotification/{id}")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getNotification(Authentication authentication, @PathVariable int id) {
        Notification notification;
        try {
            notification = manageAuthenticationAndFindNotification(authentication, id);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok(notification);
    }

    private Notification manageAuthenticationAndFindNotification(Authentication authentication, int id) {
        Customer customer = customerService.checkForLoggedCustomer(authentication);
        return findUserNotification(customer, id);
    }

    private Notification findUserNotification(Customer customer, int notificationId) {
        Notification notification =  notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification with this id:" + notificationId + " not found"));
        if (notification.getUserId() != customer.getId()) {
            throw new IllegalArgumentException("This notification does not belong to user");
        }
        return notification;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createNotification(Authentication authentication,
                                                @RequestBody NotificationCreationRequest request) {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        Optional<Customer> CustomerToSendOpt = customerRepository.findById(request.getUserId());
        if (CustomerToSendOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        if (request.getUserId() != customer.getId()) {
            Notification notification = new Notification(request.getUserId(), request.getText());
            notificationRepository.save(notification);
            return ResponseEntity.ok(notification);
        }

        return ResponseEntity.badRequest().body("You cannot send notification to yourself");
    }

    @PostMapping("/createByEmail")
    public ResponseEntity<?> createNotification(Authentication authentication,
                                                @RequestBody NotificationCreationByEmailRequest request) {
        Customer customer;
        try {
            customer = customerService.checkForLoggedCustomer(authentication);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        Optional<Customer> customerToSendOpt = customerRepository.findByEmail(request.getEmail());
        if (customerToSendOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("No user with this id found");
        }
        Customer customerToSend = customerToSendOpt.get();

        if (!request.getEmail().equals(customer.getEmail())) {
            Notification notification = new Notification(customerToSend.getId(), request.getText());
            notificationRepository.save(notification);
            return ResponseEntity.ok(notification);
        }

        return ResponseEntity.badRequest().body("You cannot send notification to yourself");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteNotification(Authentication authentication,
                                                @RequestParam int notificationId) {
        Notification notification;
        try {
            notification = manageAuthenticationAndFindNotification(authentication, notificationId);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        notificationRepository.delete(notification);
        return ResponseEntity.ok("Notification successfully deleted");
    }
}
