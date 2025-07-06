package com.PAP_team_21.flashcards.entities.folder;

import com.PAP_team_21.flashcards.AccessLevel;
import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.deck.Deck;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.folderAccessLevel.FolderAccessLevel;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "Folders")
@Getter
@Setter
@DynamicInsert
public class Folder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonView(JsonViewConfig.Public.class)
    private Integer id;

    @Column(name = "name")
    @JsonView(JsonViewConfig.Public.class)
    private String name;

    @OneToOne(mappedBy = "rootFolder", cascade = CascadeType.ALL)
    private Customer customer;

    @JsonView(JsonViewConfig.BasicStructures.class)
    @ManyToMany(mappedBy = "folders", fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE,
                        CascadeType.DETACH, CascadeType.REFRESH})
    private List<Deck> decks;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.DETACH, CascadeType.REFRESH})
    @JoinTable(
            name = "Folder_Parent",
            joinColumns = @JoinColumn(name = "child_folder_id"),
            inverseJoinColumns = @JoinColumn(name = "parent_folder_id")
    )
    @JsonView(JsonViewConfig.ExtendedStructures.class)
    private Set<Folder> parents;

    @ManyToMany(mappedBy = "parents", cascade =
            {CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.DETACH, CascadeType.REFRESH})
    @JsonView(JsonViewConfig.Public.class)
    private Set<Folder> children;

    @ManyToMany(cascade =
            {CascadeType.PERSIST, CascadeType.MERGE,
                    CascadeType.DETACH, CascadeType.REFRESH})
    @JoinTable(
            name = "Access_Levels_Folders",
            joinColumns = @JoinColumn(name = "folder_id"),
            inverseJoinColumns = @JoinColumn(name = "access_level_id")
    )
    @JsonView(JsonViewConfig.ExtendedStructures.class)
    private List<FolderAccessLevel> accessLevels;

    public AccessLevel getAccessLevel(Customer customer)
    {
        for(FolderAccessLevel fal : accessLevels)
        {
            if(fal.getCustomer().getId().equals(customer.getId()))
            {
                return fal.getAccessLevel();
            }
        }
        return null;
    }

    public Folder() {
    }
    public Folder(String name, Customer customer) {
        this.name = name;

        FolderAccessLevel al = new FolderAccessLevel(customer, AccessLevel.OWNER, this);
        if(accessLevels == null)
        {
            accessLevels = new ArrayList<>();
        }
        this.accessLevels.add(al);
    }

    public Folder(String name, Customer customer, Folder parent) {
        this.name = name;

        FolderAccessLevel al = new FolderAccessLevel(customer, AccessLevel.OWNER, this);
        if(accessLevels == null)
        {
            accessLevels = new ArrayList<>();
        }
        this.accessLevels.add(al);

        if(parents == null)
        {
            parents = new HashSet<>();
        }

        this.getParents().add(parent);
    }
    public List<Deck> getDecks(int page, int size, String sortBy, boolean ascending) throws IllegalArgumentException {
        return decks.stream()
                .skip((long) page * size)
                .sorted(Deck.comparatorBy(sortBy, ascending))
                .limit(size)
                .collect(Collectors.toList());
    }
}
