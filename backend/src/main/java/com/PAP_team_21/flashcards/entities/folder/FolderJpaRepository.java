package com.PAP_team_21.flashcards.entities.folder;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FolderJpaRepository  extends JpaRepository<Folder, Integer> {
    @Procedure(procedureName = "get_all_user_folders")
    List<Folder> findAllUserFolders(@Param("userId") Integer userId);

    @Procedure(procedureName = "get_all_user_folders")
    List<Folder> findAllUserFolders(@Param("userId") Integer userId, Pageable pageable);
}
