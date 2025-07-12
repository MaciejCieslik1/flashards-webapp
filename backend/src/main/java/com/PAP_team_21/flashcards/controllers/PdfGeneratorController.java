package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.Errors.DeckNotFoundException;
import com.PAP_team_21.flashcards.Errors.NoPermissionException;
import com.PAP_team_21.flashcards.services.PdfGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class PdfGeneratorController {
    private final PdfGeneratorService pdfGeneratorService;

    @GetMapping("/generatePdf/{id}")
    public ResponseEntity<byte[]> generatePdf(Authentication authentication, @PathVariable int id) {
        try{
            byte[] pdfBytes = pdfGeneratorService.generatePdf(authentication, id);
            HttpHeaders headers = pdfGeneratorService.generateHeaders(authentication, id);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                    .body("No user with this id found".getBytes(StandardCharsets.UTF_8));
        }
        catch (DeckNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                    .body("Deck not found".getBytes(StandardCharsets.UTF_8));
        }
        catch (NoPermissionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                    .body("You don't have access to this deck".getBytes(StandardCharsets.UTF_8));
        }
    }
}