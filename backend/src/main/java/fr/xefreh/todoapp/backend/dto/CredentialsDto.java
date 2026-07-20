package fr.xefreh.todoapp.backend.dto;

/**
 * Request body for {@code POST /api/auth/register} and {@code POST /api/auth/login}.
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
