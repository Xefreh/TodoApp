package fr.xefreh.todoapp.backend.service;

/**
 * Logique d'inscription et de connexion. Interface isolée de l'implémentation
 * ({@link AuthServiceImpl}) afin d'être testée avec des dépendances mockées
 * (UserRepository, PasswordHasher, TokenService).
 */
public interface AuthService {

    /**
     * Crée un nouvel utilisateur.
     *
     * @throws UsernameTakenException si le nom d'utilisateur existe déjà
     */
    AuthResult register(String username, String password);

    /**
     * Authentifie un utilisateur existant.
     *
     * @throws InvalidCredentialsException si le nom est inconnu ou le mot de passe incorrect
     */
    AuthResult login(String username, String password);
}
