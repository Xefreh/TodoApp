package fr.xefreh.todoapp.backend.service;

/**
 * Signature et vérification des jetons d'authentification (JWT). Interface isolée
 * de l'implémentation (jjwt) afin d'être mockable dans les tests unitaires.
 */
public interface TokenService {

    /** Génère un jeton signé portant l'identifiant de l'utilisateur. */
    String issueFor(long userId);

    /** Vérifie la signature d'un jeton et renvoie l'identifiant utilisateur qu'il porte. */
    long verify(String token);
}
