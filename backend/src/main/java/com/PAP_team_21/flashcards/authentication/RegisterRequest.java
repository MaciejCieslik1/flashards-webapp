package com.PAP_team_21.flashcards.authentication;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {
    /*
    represents registration request for new users
     */
    private String username;
    private String email;
    private String password;
}
