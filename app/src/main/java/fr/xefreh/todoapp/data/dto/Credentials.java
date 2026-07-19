package fr.xefreh.todoapp.data.dto;

/** Corps de requête pour {@code POST /api/auth/register} et {@code POST /api/auth/login}. */
public class Credentials {
    public String username;
    public String password;

    public Credentials() {
    }

    public Credentials(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
