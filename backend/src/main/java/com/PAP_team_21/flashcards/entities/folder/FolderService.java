package com.PAP_team_21.flashcards.entities.folder;

import com.PAP_team_21.flashcards.AccessLevel;
import com.PAP_team_21.flashcards.UserAnswer;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.deck.Deck;
import com.PAP_team_21.flashcards.entities.flashcard.Flashcard;
import com.PAP_team_21.flashcards.entities.flashcardProgress.FlashcardProgress;
import com.PAP_team_21.flashcards.entities.flashcardProgress.FlashcardProgressRepository;
import com.PAP_team_21.flashcards.entities.folderAccessLevel.FolderAccessLevel;
import com.PAP_team_21.flashcards.entities.folderAccessLevel.FolderAccessLevelRepository;
import com.PAP_team_21.flashcards.entities.reviewLog.ReviewLog;
import com.PAP_team_21.flashcards.entities.reviewLog.ReviewLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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

    public boolean hasFolder(Folder folder)
    {
        return hasFolder(folder.getId());
    }

    public boolean hasFolder(int id)
    {
        Optional<Folder> found = folderRepository.findById(id);
        return found.isPresent();
    }

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

    public List<Folder> findAllUserFolders(int userId, Pageable pageable) {
        return folderRepository.findAllUserFolders(userId, pageable);
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
}

