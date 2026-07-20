package fr.xefreh.todoapp.data.dto;

/** Response of a successful registration or login: the JWT token and the user id. */
public class AuthResponse {
    public String token;
    public long userId;

    public AuthResponse() {
    }
}
