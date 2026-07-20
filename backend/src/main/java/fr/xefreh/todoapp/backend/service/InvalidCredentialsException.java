package fr.xefreh.todoapp.backend.service;

/** Thrown on login if the user does not exist or the password is incorrect. HTTP 401. */
public class InvalidCredentialsException extends AuthException {
    public InvalidCredentialsException() {
        super(401, "INVALID_CREDENTIALS", "Invalid username or password");
    }
}
