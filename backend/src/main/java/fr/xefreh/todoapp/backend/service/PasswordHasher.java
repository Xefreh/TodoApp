package fr.xefreh.todoapp.backend.service;

/**
 * Hachage et vérification de mots de passe. Interface isolée de l'implémentation
 * (argon2id) afin d'être mockable dans les tests unitaires du service.
 */
public interface PasswordHasher {

    /** Calcule le hash d'un mot de passe en clair. */
    String hash(String plainPassword);

    /** Vérifie qu'un mot de passe en clair correspond à un hash précédemment calculé. */
    boolean verify(String plainPassword, String hashedPassword);
}
