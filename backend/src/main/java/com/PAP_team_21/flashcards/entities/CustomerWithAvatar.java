package com.PAP_team_21.flashcards.entities;

import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerWithAvatar {
    @JsonView(JsonViewConfig.Public.class)
    private Customer customer;
    @JsonView(JsonViewConfig.Public.class)
    private byte[] avatar;
}
