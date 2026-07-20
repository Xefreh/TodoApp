package fr.xefreh.todoapp.backend.service;

/** Thrown on registration if the password does not meet the minimum length. HTTP 400. */
public class WeakPasswordException extends AuthException {
    public WeakPasswordException(int minLength) {
        super(400, "WEAK_PASSWORD", "Password must be at least " + minLength + " characters");
    }
}
