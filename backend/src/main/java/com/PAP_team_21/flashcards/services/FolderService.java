package com.PAP_team_21.flashcards.services;

import com.PAP_team_21.flashcards.AccessLevel;
import com.PAP_team_21.flashcards.Errors.NoPermissionException;
import com.PAP_team_21.flashcards.UserAnswer;
import com.PAP_team_21.flashcards.authentication.ResourceAccessLevelService.FolderAccessServiceResponse;
import com.PAP_team_21.flashcards.authentication.ResourceAccessLevelService.ResourceAccessService;
import com.PAP_team_21.flashcards.controllers.DTO.DeckDTO;
import com.PAP_team_21.flashcards.controllers.DTO.FolderBasicDTO;
import com.PAP_team_21.flashcards.controllers.DTOMappers.DeckMapper;
import com.PAP_team_21.flashcards.controllers.DTOMappers.FolderMapper;
import com.PAP_team_21.flashcards.controllers.requests.FolderCreationRequest;
import com.PAP_team_21.flashcards.controllers.requests.FolderUpdateRequest;
import com.PAP_team_21.flashcards.controllers.requests.ShareFolderRequest;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerRepository;
import com.PAP_team_21.flashcards.entities.deck.Deck;
import com.PAP_team_21.flashcards.entities.flashcard.Flashcard;
import com.PAP_team_21.flashcards.entities.flashcardProgress.FlashcardProgress;
import com.PAP_team_21.flashcards.entities.flashcardProgress.FlashcardProgressRepository;
import com.PAP_team_21.flashcards.entities.folder.Folder;
import com.PAP_team_21.flashcards.entities.folder.FolderJpaRepository;
import com.PAP_team_21.flashcards.entities.folderAccessLevel.FolderAccessLevel;
import com.PAP_team_21.flashcards.entities.folderAccessLevel.FolderAccessLevelRepository;
import com.PAP_team_21.flashcards.entities.reviewLog.ReviewLog;
import com.PAP_team_21.flashcards.entities.reviewLog.ReviewLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final FolderJpaRepository folderRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final FlashcardProgressRepository flashcardProgressRepository;
    private final FolderAccessLevelRepository folderAccessLevelRepository;
    private final CustomerRepository customerRepository;
    private final ResourceAccessService resourceAccessService;
    private final DeckService deckService;

    public Optional<Folder> findById(int id) {
        return folderRepository.findById(id);

    }
    public void delete(int folderId)
    {
        folderRepository.deleteById(folderId);
    }

    public Folder save(Folder folder) {
        return folderRepository.save(folder);
    }

    @Transactional
    public List<Folder> findAllUserFolders(int userId) {
        return folderRepository.findAllUserFolders(userId);
    }

    public void prepareFolderToShare(Folder folder, Customer customer, AccessLevel accessLevel) {
        List<FolderAccessLevel> folderAccessLevels = folder.getAccessLevels();
        Set<Folder> currentParents = folder.getParents();
        FolderAccessLevel folderAccessLevel = new FolderAccessLevel(customer, accessLevel,
                folder);
        currentParents.add(customer.getRootFolder());
        folder.setParents(currentParents);
        folderAccessLevels.add(folderAccessLevel);
        folder.setAccessLevels(folderAccessLevels);
        save(folder);
        folderAccessLevelRepository.save(folderAccessLevel);
        createFirstReviewAndProgress(folder, customer);
    }


    public void createFirstReviewAndProgress(Folder folder, Customer customer) {
        for (Deck deck : folder.getDecks()) {
            for (Flashcard flashcard : deck.getFlashcards()) {
                ReviewLog firstReviewLog = new ReviewLog(flashcard, customer, UserAnswer.FORGOT);
                reviewLogRepository.save(firstReviewLog);

                FlashcardProgress firstFlashcardProgress = new FlashcardProgress(flashcard, customer, firstReviewLog);
                flashcardProgressRepository.save(firstFlashcardProgress);

            }
        }
    }

    public Folder getFolderStructure(Authentication authentication) {

        String email = authentication.getName();
        Optional<Customer> customer = customerRepository.findByEmail(email);

        if(customer.isPresent()) {
            return customer.get().getRootFolder();
        }
        else {
            throw new IllegalArgumentException("No user with this id found");
        }
    }

    @Transactional
    public List<FolderBasicDTO> getAllFolders(Authentication authentication) {
        String email = authentication.getName();
        FolderMapper folderMapper = new FolderMapper();
        Optional<Customer> customer = customerRepository.findByEmail(email);
        if(customer.isEmpty()) {
            throw new IllegalArgumentException("No user with this id found");
        }

        List<Folder> folders = this.findAllUserFolders(customer.get().getId());
        return folderMapper.toDTO(folders);
    }

    public String createFolder(Authentication authentication, FolderCreationRequest request) {
        FolderAccessServiceResponse response  = resourceAccessService.getFolderAccessLevel(authentication,
                request.getParentId());

        Folder parentFolder = response.getFolder();
        AccessLevel userAccessLevel = response.getAccessLevel();
        Customer customer = response.getCustomer();

        if(userAccessLevel != null && (userAccessLevel.equals(AccessLevel.EDITOR) || userAccessLevel.equals(AccessLevel.OWNER)))
        {
            Folder folder = new Folder(request.getName(), customer, parentFolder);
            this.save(folder);
            return "folder created!";
        }
        else {
            throw new NoPermissionException("You do not have permission to create a folder here");
        }
    }

    public String updateFolder(Authentication authentication, FolderUpdateRequest request) {
        FolderAccessServiceResponse response = resourceAccessService.getFolderAccessLevel(authentication,
                request.getId());

        if(response.getAccessLevel() != null && (response.getAccessLevel().equals(AccessLevel.OWNER) ||
                response.getAccessLevel().equals(AccessLevel.EDITOR)))
        {
            Folder folder = response.getFolder();
            folder.setName(request.getName());
            this.save(folder);
            return "folder updated";
        }

        throw new NoPermissionException("You do not have permission to update this folder");
    }

    public String deleteFolder(Authentication authentication, int folderId) {
        FolderAccessServiceResponse response = resourceAccessService.getFolderAccessLevel(authentication,
                folderId);

        if(response.getFolder().equals(response.getCustomer().getRootFolder()))
            throw new IllegalArgumentException("You cannot delete the root folder");

        if(response.getAccessLevel() != null && response.getAccessLevel().equals(AccessLevel.OWNER))
        {
            this.delete(folderId);
            return "folder deleted";
        }
        else {
            throw new NoPermissionException("You do not have permission to delete this folder");
        }
    }

    public Folder getRootFolder(Authentication authentication) {
        String email = authentication.getName();
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);
        if(customerOpt.isEmpty()) {
            throw new IllegalArgumentException("No user with this id found");
        }
        Customer customer = customerOpt.get();
        return customer.getRootFolder();
    }

    public String shareFolder(Authentication authentication, ShareFolderRequest request) {
        FolderAccessServiceResponse response = resourceAccessService.getFolderAccessLevel(authentication, request.getFolderId());
        AccessLevel al = response.getAccessLevel();

        if(al.equals(AccessLevel.EDITOR) || al.equals(AccessLevel.OWNER) || al.equals(AccessLevel.VIEWER)) {
            Optional<Customer> friendOpt = customerRepository.findByEmail(request.getAddresseeEmail());
            if (friendOpt.isPresent()) {
                this.prepareFolderToShare(response.getFolder(), friendOpt.get(), response.getAccessLevel());
                return "Folder shared successfully";
            }
            else {
                throw new IllegalArgumentException("Friend not found");
            }
        }
        else {
            throw new NoPermissionException("You do not have permission to share this folder");
        }
    }

    public List<Deck> getAllDecks(Authentication authentication, int folderId) {
        FolderAccessServiceResponse response = resourceAccessService.getFolderAccessLevel(authentication,
                folderId);
        AccessLevel al = response.getAccessLevel();
        Folder folder = response.getFolder();

        if(al != null && (al.equals(AccessLevel.EDITOR) || al.equals(AccessLevel.OWNER))) {
            return folder.getDecks();
        }
        else {
            throw new NoPermissionException("You do not have permission to view this folder");
        }
    }

    public List<DeckDTO> getAllDecksInfo(Authentication authentication, int folderId) {
        DeckMapper deckMapper = new DeckMapper(deckService);
        FolderAccessServiceResponse response = resourceAccessService.getFolderAccessLevel(authentication,
                folderId);

        AccessLevel al = response.getAccessLevel();
        Folder folder = response.getFolder();

        if(al != null && (al.equals(AccessLevel.EDITOR) || al.equals(AccessLevel.OWNER)))
        {
            List<Deck> decks = folder.getDecks();
            return deckMapper.toDTO(response.getCustomer(), decks);
        }
        else {
            throw new NoPermissionException("You do not have permission to view this folder");
        }
    }

    public List<Deck> getDecks(Authentication authentication, int folderId, int page, int size,
                               String sortBy, boolean ascending) {
        FolderAccessServiceResponse response = resourceAccessService.getFolderAccessLevel(authentication,
                folderId);
        AccessLevel al = response.getAccessLevel();
        Folder folder = response.getFolder();
        if(al != null && (al.equals(AccessLevel.EDITOR) || al.equals(AccessLevel.OWNER)))
        {
            return folder.getDecks(page, size, sortBy, ascending);
        }
        else {
            throw new NoPermissionException("You do not have permission to view this folder");
        }
    }

    public List<DeckDTO> getDecksInfo(Authentication authentication, int folderId, int page, int size,
                                      String sortBy, boolean ascending) {
        DeckMapper deckMapper = new DeckMapper(deckService);
        FolderAccessServiceResponse response = resourceAccessService.getFolderAccessLevel(authentication,
                folderId);
        AccessLevel al = response.getAccessLevel();
        Folder folder = response.getFolder();

        if(al != null && (al.equals(AccessLevel.EDITOR) || al.equals(AccessLevel.OWNER)))
        {
            List<Deck> decks = folder.getDecks(page, size, sortBy, ascending);
            return deckMapper.toDTO(response.getCustomer(), decks);
        }
        else {
            throw new NoPermissionException("You do not have permission to view this folder");
        }
    }

    public Set<Folder> getFoldersChildren(Authentication authentication, int folderId) {
        FolderAccessServiceResponse response = resourceAccessService.getFolderAccessLevel(authentication,
                folderId);

        Folder folder = response.getFolder();
        AccessLevel al = response.getAccessLevel();
        if(al != null) {
            return folder.getChildren();
        }
        else {
            throw new NoPermissionException("You do not have permission to view this folder");
        }
    }

    public List<FolderAccessLevel> getFoldersAccessLevel(Authentication authentication, int folderId) {
        FolderAccessServiceResponse response = resourceAccessService.getFolderAccessLevel(authentication,
                folderId);
        Folder folder = response.getFolder();
        AccessLevel al = response.getAccessLevel();

        if(al != null) {
            return folder.getAccessLevels();
        }
        else {
            throw new NoPermissionException("You do not have permission to view access levels of this folder");
        }
    }

    public Folder getFolder(Authentication authentication, int folderId) {
        FolderAccessServiceResponse response = resourceAccessService.getFolderAccessLevel(authentication,
                folderId);
        Folder folder = response.getFolder();
        AccessLevel al = response.getAccessLevel();

        if(al != null) {
            return folder;
        }
        else {
            throw new NoPermissionException("You do not have permission to view this folder");
        }
    }
}

