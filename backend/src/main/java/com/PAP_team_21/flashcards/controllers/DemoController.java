package com.PAP_team_21.flashcards.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/demo")
public class DemoController {
    @GetMapping("/this-is-secure")
    public ResponseEntity<String> secured(Authentication authentication)
    {
        String email = (String)(authentication).getPrincipal();
        return ResponseEntity.ok("hello, your email is: " + email);
    }
}
