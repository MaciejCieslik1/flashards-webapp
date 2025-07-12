package com.PAP_team_21.flashcards.Errors;

public class NoPermissionException extends RuntimeException {
  public NoPermissionException(String message) {
    super(message);
  }
}
