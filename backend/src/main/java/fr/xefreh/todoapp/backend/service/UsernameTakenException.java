package fr.xefreh.todoapp.backend.service;

/** Levée lors de l'inscription si le nom d'utilisateur existe déjà. HTTP 409. */
public class UsernameTakenException extends AuthException {
    public UsernameTakenException(String username) {
        super("Username already taken: " + username);
    }
}
