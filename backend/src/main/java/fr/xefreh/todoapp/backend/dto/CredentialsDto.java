package fr.xefreh.todoapp.backend.dto;

/**
 * Corps de requête pour {@code POST /api/auth/register} et {@code POST /api/auth/login}.
 */
public class CredentialsDto {
    public String username;
    public String password;

    public CredentialsDto() {
    }

    public CredentialsDto(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
