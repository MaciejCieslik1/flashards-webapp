package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.controllers.DTO.DeckDTO;
import com.PAP_team_21.flashcards.controllers.DTO.FolderBasicDTO;
import com.PAP_team_21.flashcards.controllers.requests.FolderCreationRequest;
import com.PAP_team_21.flashcards.controllers.requests.FolderUpdateRequest;
import com.PAP_team_21.flashcards.controllers.requests.ShareFolderRequest;
import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.deck.Deck;
import com.PAP_team_21.flashcards.entities.folder.Folder;
import com.PAP_team_21.flashcards.entities.folderAccessLevel.FolderAccessLevel;
import com.PAP_team_21.flashcards.services.FolderService;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/folder")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @GetMapping("/getFolderStructure")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getAllFolders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending) {
        
        try {
            Folder folder = folderService.getFolderStructure(authentication);
            return ResponseEntity.ok(folder);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getAllFolders")
    public ResponseEntity<?> getAllFolders(Authentication authentication) {
        try {
            List<FolderBasicDTO> folders = folderService.getAllFolders(authentication);
            return ResponseEntity.ok(folders);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createFolder(Authentication authentication,
                                          @RequestBody FolderCreationRequest request) {
        try {
            String communicate = folderService.createFolder(authentication, request);
            return ResponseEntity.ok(communicate);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateFolder(Authentication authentication,
                                          @RequestBody FolderUpdateRequest request) {
        try {
            String communicate = folderService.updateFolder(authentication, request);
            return ResponseEntity.ok(communicate);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFolder(Authentication authentication, @RequestParam int folderId) {
        try {
            String communicate = folderService.deleteFolder(authentication, folderId);
            return ResponseEntity.ok(communicate);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getRootFolder")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getRootFolder(Authentication authentication) {
        try {
            Folder folder = folderService.getRootFolder(authentication);
            return ResponseEntity.ok(folder);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/shareFolder")
    public ResponseEntity<?> shareFolder(Authentication authentication, @RequestBody ShareFolderRequest request) {
        try {
            String communicate = folderService.shareFolder(authentication, request);
            return ResponseEntity.ok(communicate);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getAllDecks")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getAllDecks(
            Authentication authentication,
            @RequestParam int folderId) {
        try {
            List<Deck> decks = folderService.getAllDecks(authentication, folderId);
            return ResponseEntity.ok(decks);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getAllDecksInfo")
    public ResponseEntity<?> getAllDecksInfo(
            Authentication authentication,
            @RequestParam int folderId) {
        try {
            List<DeckDTO> deckDTOs = folderService.getAllDecksInfo(authentication, folderId);
            return ResponseEntity.ok(deckDTOs);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getDecks")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getDecks(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending,
            @RequestParam int folderId) {
       try {
           List<Deck> decks = folderService.getDecks(authentication, folderId, page, size, sortBy, ascending);
           return ResponseEntity.ok(decks);
       }
       catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
       }
    }

    @GetMapping("/getDecksInfo")
    public ResponseEntity<?> getDecksInfo(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending,
            @RequestParam int folderId) {
        try {
            List<DeckDTO> deckDTOs = folderService.getDecksInfo(authentication, folderId, page, size, sortBy,
                    ascending);
            return ResponseEntity.ok(deckDTOs);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/children")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getFoldersChildren(Authentication authentication, @RequestParam int folderId) {
        try {
            Set<Folder> folders = folderService.getFoldersChildren(authentication, folderId);
            return ResponseEntity.ok(folders);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/accessLevels")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getFoldersAccessLevel(Authentication authentication, @RequestParam int folderId) {
        try {
            List<FolderAccessLevel> folderAccessLevels = folderService.getFoldersAccessLevel(authentication,
                    folderId);
            return ResponseEntity.ok(folderAccessLevels);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getFolder")
    @JsonView(JsonViewConfig.Public.class)
    public ResponseEntity<?> getFolder(Authentication authentication, @RequestParam int folderId) {
        try {
            Folder folder = folderService.getFolder(authentication, folderId);
            return ResponseEntity.ok(folder);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
