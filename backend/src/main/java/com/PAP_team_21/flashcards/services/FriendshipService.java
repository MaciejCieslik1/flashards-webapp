package com.PAP_team_21.flashcards.services;

import com.PAP_team_21.flashcards.Errors.AvatarNotFoundException;
import com.PAP_team_21.flashcards.entities.CustomerWithAvatar;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerRepository;
import com.PAP_team_21.flashcards.entities.friendship.Friendship;
import com.PAP_team_21.flashcards.entities.friendship.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final CustomerService customerService;
    private final FriendshipRepository friendshipRepository;
    private final CustomerRepository customerRepository;

    public CustomerWithAvatar getFriendshipById(Authentication authentication, int id) {
        Optional<Customer> friendOpt;
        friendOpt = manageAuthenticationAndFindFriendOpt(authentication, id);
        return this.getFriendWithAvatarResponseEntity(friendOpt);
    }

    public Optional<Customer> manageAuthenticationAndFindFriendOpt(Authentication authentication, int id) {
        Customer customer = customerService.checkForLoggedCustomer(authentication);
        Friendship friendship;
        friendship = this.getFriendship(id);
        Optional<Customer> friendOpt;
        if (friendship.getReceiverId() == customer.getId()) {
            friendOpt = customerRepository.findById(friendship.getSenderId());
        }
        else {
            friendOpt = customerRepository.findById(friendship.getReceiverId());
        }
        return friendOpt;
    }

    public Friendship getFriendship(int id) {
        return friendshipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Friendship with id " + id + " not found"));
    }

    public CustomerWithAvatar getFriendWithAvatarResponseEntity(Optional<Customer> friendOpt) {
        if (friendOpt.isPresent()) {
            Customer friend = friendOpt.get();
            byte[] avatar = friend.getAvatar();
            if (avatar == null) {
                throw new AvatarNotFoundException("Error fetching avatar");
            }
            return new CustomerWithAvatar(friend, avatar);
        }
        else {
            throw new IllegalArgumentException("No customer with this id found");
        }
    }

    public String deleteFriendship(Authentication authentication, int friendshipId) {
        Customer customer = customerService.checkForLoggedCustomer(authentication);
        Friendship friendship = getFriendship(friendshipId);

        if (friendship.getReceiverId() == customer.getId() || friendship.getSenderId() == customer.getId()) {
            friendshipRepository.delete(friendship);
            return "Friendship successfully deleted";
        }
        else {
            throw new IllegalArgumentException("User do not have access to this friendship");
        }
    }
}
