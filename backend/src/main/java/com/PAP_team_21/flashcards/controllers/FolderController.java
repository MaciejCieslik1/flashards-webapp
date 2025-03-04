package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.AccessLevel;
import com.PAP_team_21.flashcards.Errors.ResourceNotFoundException;
import com.PAP_team_21.flashcards.VerificationTokenGenerator;
import com.PAP_team_21.flashcards.authentication.AuthenticationEmailSender.AuthenticationEmailSender;
import com.PAP_team_21.flashcards.authentication.ResourceAccessLevelService.FolderAccessServiceResponse;
import com.PAP_team_21.flashcards.authentication.ResourceAccessLevelService.ResourceAccessService;
import com.PAP_team_21.flashcards.controllers.DTO.ShareFolderDTO;
import com.PAP_team_21.flashcards.controllers.DTOMappers.DeckMapper;
import com.PAP_team_21.flashcards.controllers.DTOMappers.FolderMapper;
import com.PAP_team_21.flashcards.controllers.requests.FolderCreationRequest;
import com.PAP_team_21.flashcards.controllers.requests.FolderUpdateRequest;
import com.PAP_team_21.flashcards.controllers.requests.ShareFolderRequest;
import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerRepository;
import com.PAP_team_21.flashcards.entities.deck.Deck;
import com.PAP_team_21.flashcards.entities.folder.Folder;
import com.PAP_team_21.flashcards.entities.folder.FolderDao;
import com.PAP_team_21.flashcards.entities.folder.FolderJpaRepository;
import com.PAP_team_21.flashcards.entities.folder.FolderService;
import com.PAP_team_21.flashcards.entities.folderAccessLevel.FolderAccessLevel;
import com.PAP_team_21.flashcards.entities.folderAccessLevel.FolderAccessLevelRepository;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/folder")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;
    private final CustomerRepository customerRepository;
    private final ResourceAccessService resourceAccessService;
    private final DeckMapper deckMapper;
    private final FolderMapper folderMapper;
    private final AuthenticationEmailSender emailSender;
    private final VerificationTokenGenerator tokenGenerator;
    private final FolderAccessLevelRepository folderAccessLevelRepository;

    @GetMapping("/getFolderStructure")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getAllFolders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending
    )
    {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);


        String email = authentication.getName();
        Optional<Customer> customer = customerRepository.findByEmail(email);

        if(customer.isPresent())
        {
            return ResponseEntity.ok(customer.get().getRootFolder());
        }
            // return ResponseEntity.ok(folderService.findAllByCustomer(pageable, customer.get()));
        return ResponseEntity.badRequest().body("No user with this id found");
    }

    @GetMapping("/getAllFolders")
    public ResponseEntity<?> getAllFolders(
            Authentication authentication
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "5") int size,
//            @RequestParam(defaultValue = "id") String sortBy,
//            @RequestParam(defaultValue = "true") boolean ascending
    )
    {
//        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
//        Pageable pageable = PageRequest.of(page, size, sort);

        String email = authentication.getName();
        Optional<Customer> customer = customerRepository.findByEmail(email);
        if(customer.isEmpty())
        {
            return ResponseEntity.badRequest().body("No user with this id found");
        }

        List<Folder> folders = folderService.findAllUserFolders(customer.get().getId());
        return ResponseEntity.ok(folderMapper.toDTO(folders));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createFolder(Authentication authentication,
                                          @RequestBody FolderCreationRequest request
    ) {
        FolderAccessServiceResponse response;
        try {
             response = resourceAccessService.getFolderAccessLevel(authentication, request.getParentId());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        Folder parentFolder = response.getFolder();
        AccessLevel userAccessLevel = response.getAccessLevel();
        Customer customer = response.getCustomer();

        if(userAccessLevel != null && (userAccessLevel.equals(AccessLevel.EDITOR) || userAccessLevel.equals(AccessLevel.OWNER)))
        {
            Folder folder = new Folder(request.getName(), customer, parentFolder);
            folderService.save(folder);
            return ResponseEntity.ok("folder created!");
        }
        return ResponseEntity.badRequest().body("You do not have permission to create a folder here");
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateFolder(Authentication authentication,@RequestBody FolderUpdateRequest request) {

        FolderAccessServiceResponse response;
        try {
            response = resourceAccessService.getFolderAccessLevel(authentication, request.getId());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        if(response.getAccessLevel() != null && (response.getAccessLevel().equals(AccessLevel.OWNER) || response.getAccessLevel().equals(AccessLevel.EDITOR)))
        {
            Folder folder = response.getFolder();
            folder.setName(request.getName());
            folderService.save(folder);
            return ResponseEntity.ok("folder updated");
        }

        return ResponseEntity.badRequest().body("You do not have permission to update this folder");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFolder(Authentication authentication, @RequestParam int folderId) {
        FolderAccessServiceResponse response;
        try {
            response = resourceAccessService.getFolderAccessLevel(authentication, folderId);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        if(response.getFolder().equals(response.getCustomer().getRootFolder()))
            return ResponseEntity.badRequest().body("You cannot delete the root folder");

        if(response.getAccessLevel() != null && (response.getAccessLevel().equals(AccessLevel.OWNER) || response.getAccessLevel().equals(AccessLevel.EDITOR)))
        {
            folderService.delete(folderId);
            return ResponseEntity.ok("folder deleted");
        }

        return ResponseEntity.badRequest().body("You do not have permission to delete this folder");
    }

    @GetMapping("/getRootFolder")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getRootFolder(Authentication authentication) {
        String email = authentication.getName();
        Optional<Customer> customerOpt= customerRepository.findByEmail(email);
        if(customerOpt.isEmpty())
        {
            return ResponseEntity.badRequest().body("No user with this id found");
        }
        Customer customer = customerOpt.get();

        Folder rootFolder = customer.getRootFolder();
        return ResponseEntity.ok(rootFolder);
    }

    @PostMapping("/shareFolder")
    public ResponseEntity<?> shareFolder(Authentication authentication, @RequestBody ShareFolderRequest request)
    {
        FolderAccessServiceResponse response;
        try {
            response = resourceAccessService.getFolderAccessLevel(authentication, request.getFolderId());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        AccessLevel al = response.getAccessLevel();

        if(al.equals(AccessLevel.EDITOR) || al.equals(AccessLevel.OWNER) || al.equals(AccessLevel.VIEWER)) {
            Folder folder = response.getFolder();
            List<FolderAccessLevel> folderAccessLevels = folder.getAccessLevels();
            Set<Folder> currentParents = folder.getParents();
            Optional<Customer> friendOpt = customerRepository.findByEmail(request.getAddresseeEmail());
            if (friendOpt.isPresent()) {
                FolderAccessLevel folderAccessLevel = new FolderAccessLevel(friendOpt.get(), request.getAccessLevel(),
                        folder);
                currentParents.add(friendOpt.get().getRootFolder());
                folder.setParents(currentParents);
                folderAccessLevels.add(folderAccessLevel);
                folder.setAccessLevels(folderAccessLevels);
                folderService.save(folder);
                folderAccessLevelRepository.save(folderAccessLevel);
                return ResponseEntity.ok("Folder shared successfully");
            }
            return ResponseEntity.badRequest().body("Friend not found");
        }
        return ResponseEntity.badRequest().body("You do not have permission to view this folder");
    }


    @GetMapping("/getAllDecks")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getAllDecks(
            Authentication authentication,
            @RequestParam int folderId
    )
    {
        FolderAccessServiceResponse response;
        try {
            response = resourceAccessService.getFolderAccessLevel(authentication, folderId);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        AccessLevel al = response.getAccessLevel();
        Folder folder = response.getFolder();

        if(al != null && (al.equals(AccessLevel.EDITOR) || al.equals(AccessLevel.OWNER)))
        {
            List<Deck> decks = folder.getDecks();
            return ResponseEntity.ok(decks);

        }
        return ResponseEntity.badRequest().body("You do not have permission to view this folder");
    }

    @GetMapping("/getAllDecksInfo")
    public ResponseEntity<?> getAllDecksInfo(
            Authentication authentication,
            @RequestParam int folderId
    )
    {
        FolderAccessServiceResponse response;
        try {
            response = resourceAccessService.getFolderAccessLevel(authentication, folderId);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        AccessLevel al = response.getAccessLevel();
        Folder folder = response.getFolder();

        if(al != null && (al.equals(AccessLevel.EDITOR) || al.equals(AccessLevel.OWNER)))
        {
            List<Deck> decks = folder.getDecks();
            return ResponseEntity.ok(deckMapper.toDTO(response.getCustomer(), decks));
        }
        return ResponseEntity.badRequest().body("You do not have permission to view this folder");
    }

    @GetMapping("/getDecks")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getDecks(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending,
            @RequestParam int folderId
    )
    {
        FolderAccessServiceResponse response;
        try {
            response = resourceAccessService.getFolderAccessLevel(authentication, folderId);
        }
        catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        AccessLevel al = response.getAccessLevel();
        Folder folder = response.getFolder();
        if(al != null && (al.equals(AccessLevel.EDITOR) || al.equals(AccessLevel.OWNER)))
        {
            try{
                List<Deck> decks = folder.getDecks(page, size, sortBy, ascending);
                return ResponseEntity.ok(decks);
            } catch(IllegalArgumentException e)
            {
                return ResponseEntity.badRequest().body("Invalid sort field, cannot sort by: "  + sortBy);
            }
        }
        return ResponseEntity.badRequest().body("You do not have permission to view this folder");
    }

    @GetMapping("/getDecksInfo")
    public ResponseEntity<?> getDecksInfo(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending,
            @RequestParam int folderId
    )
    {
        FolderAccessServiceResponse response;
        try {
            response = resourceAccessService.getFolderAccessLevel(authentication, folderId);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        AccessLevel al = response.getAccessLevel();
        Folder folder = response.getFolder();

        if(al != null && (al.equals(AccessLevel.EDITOR) || al.equals(AccessLevel.OWNER)))
        {
            try{
                List<Deck> decks = folder.getDecks(page, size, sortBy, ascending);
                return ResponseEntity.ok(deckMapper.toDTO(response.getCustomer(), decks));
            } catch(IllegalArgumentException e)
            {
                return ResponseEntity.badRequest().body("Invalid sort field, cannot sort by: "  + sortBy);
            }
        }
        return ResponseEntity.badRequest().body("You do not have permission to view this folder");
    }

    @GetMapping("/children")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getFoldersChildren(Authentication authentication, @RequestParam int folderId) {

        FolderAccessServiceResponse response;
        try {
            response = resourceAccessService.getFolderAccessLevel(authentication, folderId);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        Folder folder = response.getFolder();
        AccessLevel al = response.getAccessLevel();
        if(al != null)
        {
            Set<Folder> children = folder.getChildren();
            return ResponseEntity.ok(children);
        }

        return ResponseEntity.badRequest().body("You do not have permission to view this folder");
    }

    @GetMapping("/accessLevels")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getFoldersAccessLevel(Authentication authentication, @RequestParam int folderId) {
        FolderAccessServiceResponse response;
        try {
            response = resourceAccessService.getFolderAccessLevel(authentication, folderId);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        Folder folder = response.getFolder();
        AccessLevel al = response.getAccessLevel();

        if(al != null)
        {
            return ResponseEntity.ok(folder.getAccessLevels());
        }

        return ResponseEntity.badRequest().body("You do not have permission to view to view access levels of this folder");
    }

    @GetMapping("/getFolder")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getFolder(Authentication authentication, @RequestParam int folderId) {
        FolderAccessServiceResponse response;
        try {
            response = resourceAccessService.getFolderAccessLevel(authentication, folderId);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        Folder folder = response.getFolder();
        AccessLevel al = response.getAccessLevel();

        if(al != null)
        {
            return ResponseEntity.ok(folder);
        }

        return ResponseEntity.badRequest().body("You do not have permission to view this folder");
    }
}
