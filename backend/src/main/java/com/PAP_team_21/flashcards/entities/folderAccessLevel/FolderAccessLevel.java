package com.PAP_team_21.flashcards.entities.folderAccessLevel;

import com.PAP_team_21.flashcards.AccessLevel;
import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.folder.Folder;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="Folder_Access_Level")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FolderAccessLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonView(JsonViewConfig.Public.class)
    private Integer id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "customer_id")
    @JsonView(JsonViewConfig.Public.class)
    private Customer customer;

    @Column(name = "access_level")
    @JsonView(JsonViewConfig.Public.class)
    private AccessLevel accessLevel;

    @ManyToMany(mappedBy = "accessLevels", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    private List<Folder> folders;


    public FolderAccessLevel(Customer customer, AccessLevel accessLevel, Folder folder) {
        this.customer = customer;
        this.accessLevel = accessLevel;
        if(this.folders == null)
        {
            folders = new ArrayList<>();
        }

        this.folders.add(folder);
    }
}
