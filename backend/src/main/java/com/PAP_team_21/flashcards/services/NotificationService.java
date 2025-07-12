package com.PAP_team_21.flashcards.services;

import com.PAP_team_21.flashcards.controllers.requests.NotificationCreationByEmailRequest;
import com.PAP_team_21.flashcards.controllers.requests.NotificationCreationRequest;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerRepository;
import com.PAP_team_21.flashcards.entities.notification.Notification;
import com.PAP_team_21.flashcards.entities.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;

    public Notification getNotificationById(Authentication authentication, int id) {
        Customer customer = customerService.checkForLoggedCustomer(authentication);
        return findUserNotification(customer, id);
    }

    private Notification findUserNotification(Customer customer, int notificationId) {
        Notification notification =  notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification with this id:" + notificationId +
                        " not found"));
        if (notification.getUserId() != customer.getId()) {
            throw new IllegalArgumentException("This notification does not belong to user");
        }
        return notification;
    }

    public Notification createNotification(Authentication authentication, NotificationCreationRequest request) {
        Customer customer = customerService.checkForLoggedCustomer(authentication);

        Optional<Customer> CustomerToSendOpt = customerRepository.findById(request.getUserId());
        if (CustomerToSendOpt.isEmpty()) {
            throw new IllegalArgumentException("No user with this id found");
        }

        if (request.getUserId() != customer.getId()) {
            Notification notification = new Notification(request.getUserId(), request.getText());
            notificationRepository.save(notification);
            return notification;
        }
        else {
            throw new IllegalArgumentException("You cannot send notification to yourself");
        }
    }

    public Notification createNotificationByEmail(Authentication authentication,
                                                  NotificationCreationByEmailRequest request) {
        Customer customer = customerService.checkForLoggedCustomer(authentication);

        Optional<Customer> customerToSendOpt = customerRepository.findByEmail(request.getEmail());
        if (customerToSendOpt.isEmpty()) {
            throw new IllegalArgumentException("No user with this id found");
        }
        Customer customerToSend = customerToSendOpt.get();

        if (!request.getEmail().equals(customer.getEmail())) {
            Notification notification = new Notification(customerToSend.getId(), request.getText());
            notificationRepository.save(notification);
            return notification;
        }
        else {
            throw new IllegalArgumentException("You cannot send notification to yourself");
        }
    }

    public String deleteNotification(Authentication authentication, int id) {
        Customer customer = customerService.checkForLoggedCustomer(authentication);
        Notification notification = findUserNotification(customer, id);
        notificationRepository.delete(notification);
        return "Notification successfully deleted";
    }
}
