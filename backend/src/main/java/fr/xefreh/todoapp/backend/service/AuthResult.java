package fr.xefreh.todoapp.backend.service;

/**
 * Résultat d'une inscription ou d'une connexion réussie : le jeton JWT à utiliser
 * pour les requêtes authentifiées, et l'identifiant de l'utilisateur.
 */
public record AuthResult(String token, long userId) {
}
