package com.antoniosandu.mindtrench.exception;
import com.antoniosandu.mindtrench.dto.response.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice

public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<MessageResponse> handleAuthentication(
            AuthenticationException ex) {

        MessageResponse error =
                new MessageResponse(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<MessageResponse> handleUserNotFound(
            UserNotFoundException ex) {

        MessageResponse error =
                new MessageResponse(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<MessageResponse> handleUsernameAlreadyExists(
            UsernameAlreadyExistsException ex) {

        MessageResponse error =
                new MessageResponse(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<MessageResponse> handlePasswordMismatch(
            PasswordMismatchException ex) {

        MessageResponse error =
                new MessageResponse(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<MessageResponse> handleGameNotFound(
            GameNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(MaxGamesReachedException.class)
    public ResponseEntity<MessageResponse> handleMaxGamesReached(
            MaxGamesReachedException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(InvalidItemException.class)
    public ResponseEntity<MessageResponse> handleInvalidItemException(
            InvalidItemException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(InvalidTurnException.class)
    public ResponseEntity<MessageResponse> handleInvalidTUrnException(
            InvalidTurnException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(ex.getMessage()));
    }
}

