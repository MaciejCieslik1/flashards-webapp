package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.controllers.requests.NotificationCreationByEmailRequest;
import com.PAP_team_21.flashcards.controllers.requests.NotificationCreationRequest;
import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerRepository;
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

    @GetMapping("/getNotification/{id}")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getNotification(Authentication authentication, @PathVariable int id) {
        String email = authentication.getName();
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);
        if(customerOpt.isEmpty())
        {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        Optional<Notification> notificationOpt = notificationRepository.findById(id);
        if (notificationOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("No notification with this id found");
        }
        Notification notification = notificationOpt.get();

        if (notification.getUserId() != customerOpt.get().getId()) {
            return ResponseEntity.badRequest().body("This notification does not belong to user");
        }

        return ResponseEntity.ok(notification);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createNotification(Authentication authentication,
                                                @RequestBody NotificationCreationRequest request) {
        String email = authentication.getName();
        Optional<Customer> customerOpt= customerRepository.findByEmail(email);
        if(customerOpt.isEmpty())
        {
            return ResponseEntity.badRequest().body("No user with this id found");
        }
        Customer customer = customerOpt.get();

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
        String email = authentication.getName();
        Optional<Customer> customerOpt= customerRepository.findByEmail(email);
        if(customerOpt.isEmpty())
        {
            return ResponseEntity.badRequest().body("No user with this id found");
        }
        Customer customer = customerOpt.get();

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
        String email = authentication.getName();
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);
        if (customerOpt.isEmpty())
        {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("No notification with this id found");
        }
        Notification notification = notificationOpt.get();

        if (notification.getUserId() != customerOpt.get().getId()) {
            return ResponseEntity.badRequest().body("This notification does not belong to user");
        }

        notificationRepository.delete(notification);
        return ResponseEntity.ok("Notification successfully deleted");
    }
}
