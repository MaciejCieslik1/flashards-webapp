package com.PAP_team_21.flashcards.services;

import com.PAP_team_21.flashcards.Errors.AvatarNotFoundException;
import com.PAP_team_21.flashcards.controllers.requests.UpdateBioRequest;
import com.PAP_team_21.flashcards.controllers.requests.UpdateUsernameRequest;
import com.PAP_team_21.flashcards.entities.CustomerWithAvatar;
import com.PAP_team_21.flashcards.entities.FriendshipResponse;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerDao;
import com.PAP_team_21.flashcards.entities.customer.CustomerRepository;
import com.PAP_team_21.flashcards.entities.folder.FolderJpaRepository;
import com.PAP_team_21.flashcards.entities.friendship.Friendship;
import com.PAP_team_21.flashcards.entities.friendship.FriendshipRepository;
import com.PAP_team_21.flashcards.entities.notification.Notification;
import com.PAP_team_21.flashcards.entities.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerDao customerDaoImpl;
    private final CustomerRepository customerRepository;
    private final FolderJpaRepository folderJpaRepository;
    private final NotificationRepository notificationRepository;
    private final FriendshipRepository friendshipRepository;

    public List<Customer> findByUsername(String username) {
        return customerDaoImpl.findUserByUsername(username);
    }

    public Customer checkForLoggedCustomer(Authentication authentication) {
        String email = authentication.getName();
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer with email " + email + " not found"));
    }

    public Optional<Customer> findCustomerById(int id) {
        return customerRepository.findById(id);
    }

    public Optional<Customer> findCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public void updateUsername(Customer customer, UpdateUsernameRequest request) {
        String newUsername = request.getNewUsername();
        if(newUsername.trim().isEmpty())
        {
            throw new IllegalArgumentException ("Username cannot be empty");
        }

        customer.setUsername(newUsername);
        customerRepository.save(customer);
    }

    public void updateBio(Customer customer, UpdateBioRequest request) {
        String newBio = request.getBio();
        if(newBio.trim().isEmpty())
        {
            throw new IllegalArgumentException("Biography cannot be empty");
        }

        customer.setBio(newBio);
        customerRepository.save(customer);
    }

    public void updateAvatar(Customer customer, MultipartFile avatar) throws IOException{
        String newAvatarPath = "/app/avatars/new_profile_picture.jpg";

        File avatarFile = new File(newAvatarPath);
        try {
            avatar.transferTo(avatarFile);
        }
        catch (IOException e) {
            throw new IOException("Failed to upload avatar:" + e.getMessage());
        }

        customer.setProfilePicturePath(newAvatarPath);
        customerRepository.save(customer);
    }

    public void deleteCustomer(Customer customer) {
        folderJpaRepository.delete(customer.getRootFolder());
        customerRepository.delete(customer);
    }

    public CustomerWithAvatar getSelf(Customer customer) {
        byte[] avatar = customer.getAvatar();
        if (avatar != null) {
            return new CustomerWithAvatar(customer, avatar);
        }
        else {
            throw new AvatarNotFoundException("Avatar not found for user");
        }
    }

    public List<Friendship> getReceivedFriendships(Customer customer) {
        List<Friendship> receivedFriendships = customer.getReceivedFriendships();
        List<Friendship> waitingReceivedFriendships = new ArrayList<>();
        for (Friendship friendship : receivedFriendships) {
            if (!friendship.isAccepted()) {
                waitingReceivedFriendships.add(friendship);
            }
        }
        return waitingReceivedFriendships;
    }

    public List<Friendship> getSentFriendships(Customer customer) {
        return customer.getSentFriendships();
    }

    public List<Notification> getNotifications(Customer customer) {
        return customer.getNotifications();
    }

    public List<Customer> getFriends(Customer customer) {
        List<Customer> friends = new ArrayList<>();

        List<Friendship> possibleFriendships = customer.getSentFriendships();

        for (Friendship friendship : possibleFriendships) {
            if (friendship.isAccepted()) {
                Customer friend = friendship.getReceiver();
                friends.add(friend);
            }
        }

        possibleFriendships = customer.getReceivedFriendships();
        for (Friendship friendship : possibleFriendships) {
            if (friendship.isAccepted()) {
                Customer friend = friendship.getSender();
                friends.add(friend);
            }
        }
        return friends;
    }

    public CustomerWithAvatar getFriendById(Customer customer, int id) {
        Optional<Customer> customerToFindOpt = customerRepository.findById(id);
        if (customerToFindOpt.isEmpty())
        {
            throw new IllegalArgumentException("No friend with this id found");
        }
        return findFriend(customer, customerToFindOpt.get());
    }

    private CustomerWithAvatar findFriend(Customer customer, Customer customerToFind) {
        int idOfWantedPerson = customerToFind.getId();
        Customer friend = findFriendInSentList(customer, idOfWantedPerson);
        if (friend == null) {
            friend = findFriendInReceivedList(customer, idOfWantedPerson);
        }
        if (friend == null) {
            throw new IllegalArgumentException("No friend with this id found");
        }
        return fetchAvatar(friend);
    }

    private Customer findFriendInSentList(Customer customer, int idOfWantedPerson) {
        List<Friendship> possibleFriendships = customer.getSentFriendships();
        for (Friendship friendship : possibleFriendships) {
            if (friendship.getReceiverId() == idOfWantedPerson && friendship.isAccepted()) {
                return friendship.getReceiver();
            }
        }
        return null;
    }

    private Customer findFriendInReceivedList(Customer customer, int idOfWantedPerson) {
        List<Friendship> possibleFriendships = customer.getReceivedFriendships();
        for (Friendship friendship : possibleFriendships) {
            if (friendship.getReceiverId() == idOfWantedPerson && friendship.isAccepted()) {
                return friendship.getSender();
            }
        }
        return null;
    }

    private CustomerWithAvatar fetchAvatar(Customer friend) {
        String avatarPath = friend.getProfilePicturePath();
        Path avatarFilePath = Paths.get("/app/avatars", avatarPath);
        try {
            byte[] avatarBytes = Files.readAllBytes(avatarFilePath);
                return new CustomerWithAvatar(friend, avatarBytes);
        }
        catch (IOException e) {
            throw new AvatarNotFoundException("Failed to fetch avatar");
        }
    }

    public CustomerWithAvatar getFriendByEmail(Customer customer, String email) {
        Optional<Customer> customerToFindOpt = customerRepository.findByEmail(email);
        if (customerToFindOpt.isEmpty())
        {
            throw new IllegalArgumentException("No friend with this email found");
        }
        return findFriend(customer, customerToFindOpt.get());
    }

    public FriendshipResponse sendFriendshipOfferById(Customer customer, int id) {
        Notification notification = createInvitationNotification(customer, id);
        Friendship friendship = new Friendship(customer.getId(), id, false);
        FriendshipResponse friendshipResponse = new FriendshipResponse(friendship, notification);

        notificationRepository.save(notification);
        friendshipRepository.save(friendship);
        return friendshipResponse;
    }

    private Notification createInvitationNotification(Customer customer, int id) {
        Optional<Customer> customerToAddOpt = customerRepository.findById(id);
        if (customerToAddOpt.isEmpty()) {
            throw new IllegalArgumentException("No friend with this id found");
        }

        if (customerToAddOpt.get().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("You cannot send friendship request to yourself");
        }

        String invitationText = "User with Id: " + customer.getId() + ", email: " + customer.getEmail() +
                " username: " + customer.getUsername() + " has sent you the friend request.";
        return new Notification(id, true, invitationText);
    }

    public FriendshipResponse sendFriendshipOfferByEmail(Customer customer, String email) {
        Optional<Customer> customerToAddOpt = customerRepository.findByEmail(email);
        if (customerToAddOpt.isEmpty()) {
            throw new IllegalArgumentException("No friend with this email found");
        }
        Customer customerToAdd = customerToAddOpt.get();
        Notification notification = createInvitationNotification(customer, customerToAdd.getId());
        Friendship friendship = new Friendship(customer.getId(), customerToAdd.getId(), false);
        FriendshipResponse friendshipResponse = new FriendshipResponse(friendship, notification);

        notificationRepository.save(notification);
        friendshipRepository.save(friendship);
        return friendshipResponse;
    }

    public Friendship acceptFriendshipOfferById(Customer customer, int id) {
        Optional<Friendship> friendshipOpt = friendshipRepository.findById(id);
        if (friendshipOpt.isEmpty()) {
            throw new IllegalArgumentException("No friendship with this id found");
        }
        Friendship friendship = friendshipOpt.get();

        if (customer.getId() != friendship.getReceiverId()) {
            throw new IllegalArgumentException("You are not the receiver of the friendship, you cannot accept it.");
        }

        friendship.setAccepted(true);
        friendshipRepository.save(friendship);

        return friendship;
    }
}
