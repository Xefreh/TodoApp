package fr.xefreh.todoapp.backend.service;

/**
 * Registration and login logic. Interface kept separate from the implementation
 * ({@link AuthServiceImpl}) so it can be tested with mocked dependencies
 * (UserRepository, PasswordHasher, TokenService).
 */
public interface AuthService {

    /**
     * Creates a new user.
     *
     * @throws UsernameTakenException if the username already exists
     */
    AuthResult register(String username, String password);

    /**
     * Authenticates an existing user.
     *
     * @throws InvalidCredentialsException if the name is unknown or the password is incorrect
     */
    AuthResult login(String username, String password);
}
