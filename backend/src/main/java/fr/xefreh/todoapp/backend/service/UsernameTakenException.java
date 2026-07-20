package fr.xefreh.todoapp.backend.service;

/** Thrown on registration if the username already exists. HTTP 409. */
public class UsernameTakenException extends AuthException {
    public UsernameTakenException(String username) {
        super(409, "USERNAME_TAKEN", "Username already taken: " + username);
    }
}
