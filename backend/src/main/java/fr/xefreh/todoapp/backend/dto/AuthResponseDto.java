package fr.xefreh.todoapp.backend.dto;

/**
 * Response body of a successful registration or login.
 */
public class AuthResponseDto {
    public String token;
    public long userId;

    public AuthResponseDto() {
    }

    public AuthResponseDto(String token, long userId) {
        this.token = token;
        this.userId = userId;
    }
}
