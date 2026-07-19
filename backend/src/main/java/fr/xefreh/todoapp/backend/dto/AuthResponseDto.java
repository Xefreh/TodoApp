package fr.xefreh.todoapp.backend.dto;

/**
 * Corps de réponse d'une inscription ou connexion réussie.
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
