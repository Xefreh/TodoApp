package fr.xefreh.todoapp.backend.service;

/** Levée lors du login si l'utilisateur n'existe pas ou si le mot de passe est incorrect. HTTP 401. */
public class InvalidCredentialsException extends AuthException {
    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
}
