package fr.xefreh.todoapp.data.dto;

/** Réponse d'une inscription ou connexion réussie : le jeton JWT et l'id utilisateur. */
public class AuthResponse {
    public String token;
    public long userId;

    public AuthResponse() {
    }
}
