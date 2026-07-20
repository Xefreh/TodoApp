package fr.xefreh.todoapp.data.dto;

/** Request body for {@code POST /api/auth/register} and {@code POST /api/auth/login}. */
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
