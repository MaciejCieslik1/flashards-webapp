package com.PAP_team_21.flashcards.entities.customer;

import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.authority.Authority;
import com.PAP_team_21.flashcards.entities.flashcardProgress.FlashcardProgress;
import com.PAP_team_21.flashcards.entities.folder.Folder;
import com.PAP_team_21.flashcards.entities.folderAccessLevel.FolderAccessLevel;
import com.PAP_team_21.flashcards.entities.friendship.Friendship;
import com.PAP_team_21.flashcards.entities.login.Login;
import com.PAP_team_21.flashcards.entities.notification.Notification;
import com.PAP_team_21.flashcards.entities.reviewLog.ReviewLog;
import com.PAP_team_21.flashcards.entities.sentVerificationCodes.SentVerificationCode;
import com.PAP_team_21.flashcards.entities.userPreferences.UserPreferences;
import com.PAP_team_21.flashcards.entities.userStatistics.UserStatistics;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Customers")
@Getter
@Setter
@NoArgsConstructor
@DynamicInsert
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonView(JsonViewConfig.Public.class)
    private Integer id;

    @Column(name = "email")
    @JsonView(JsonViewConfig.Public.class)
    private String email;

    @Column(name = "password_hash")
    @JsonView(JsonViewConfig.Secure.class)
    private String passwordHash;

    @Column(name = "username")
    @JsonView(JsonViewConfig.Public.class)
    private String username;

    @Column(name = "account_expired")
    @JsonView(JsonViewConfig.Internal.class)
    private boolean accountExpired;

    @Column(name = "account_locked")
    @JsonView(JsonViewConfig.Internal.class)
    private boolean accountLocked;

    @Column(name = "credentials_expired")
    @JsonView(JsonViewConfig.Internal.class)
    private boolean credentialsExpired;

    @Column(name = "enabled")
    @JsonView(JsonViewConfig.Internal.class)
    private boolean enabled;

    @Column(name = "profile_creation_date")
    @JsonView(JsonViewConfig.Internal.class)
    private LocalDateTime profileCreationDate;

    @Column(name = "profile_picture_path")
    @JsonView(JsonViewConfig.Internal.class)
    private String profilePicturePath;
    @OneToOne(mappedBy="customer", cascade = CascadeType.ALL)
    private SentVerificationCode sentVerificationCode;

    @Column(name = "bio")
    @JsonView(JsonViewConfig.Public.class)
    private String bio;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL)
    private UserPreferences userPreferences;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL)
    private UserStatistics userStatistics;

    @OneToMany(mappedBy = "sender", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Friendship> sentFriendships;

    @OneToMany(mappedBy = "receiver", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Friendship> receivedFriendships;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Notification> notifications;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<FlashcardProgress> flashcardProgresses;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ReviewLog> reviewLogs;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Login> logins;

    @ManyToMany(mappedBy = "customers", fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE,
                        CascadeType.DETACH, CascadeType.REFRESH})
    private List<Authority> authorities;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name="root_folder_id")
    private Folder rootFolder;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<FolderAccessLevel> folderAccessLevels;

    public Customer(String email, String username, String passwordHash) {
        this.email = email;
        this.username = username;
        this.bio = "";
        this.passwordHash = passwordHash;
        this.profileCreationDate = LocalDateTime.now();
        this.rootFolder = new Folder("ROOT", this);
        this.profilePicturePath = "/app/avatars/default-avatar.png";

        if(folderAccessLevels == null) {
            folderAccessLevels = new ArrayList<>();
        }

        this.folderAccessLevels.add(this.rootFolder.getAccessLevels().get(0));
    }

    public Customer(String email, String username, String passwordHash, String profilePicturePath) {
        this.email = email;
        this.username = username;
        this.bio = "";
        this.passwordHash = passwordHash;
        this.profileCreationDate = LocalDateTime.now();
        this.rootFolder = new Folder("ROOT", this);
        this.profilePicturePath = profilePicturePath;

        if(folderAccessLevels == null) {
            folderAccessLevels = new ArrayList<>();
        }

        this.folderAccessLevels.add(this.rootFolder.getAccessLevels().get(0));
    }


    public Customer(String email, String passwordHash, String username, String biography,
                    boolean accountExpired,  boolean accountLocked, boolean credentialsExpired,
                    boolean enabled, LocalDateTime profileCreationDate, String profilePicturePath) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.username = username;
        this.bio = biography;
        this.accountExpired = accountExpired;
        this.accountLocked = accountLocked;
        this.credentialsExpired = credentialsExpired;
        this.enabled = enabled;
        this.profileCreationDate = profileCreationDate;
        this.profilePicturePath = profilePicturePath;
        this.rootFolder = new Folder("ROOT", this);

        if(folderAccessLevels == null) {
            folderAccessLevels = new ArrayList<>();
        }
        this.folderAccessLevels.add(this.rootFolder.getAccessLevels().get(0));
    }
}
