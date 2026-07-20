package fr.xefreh.todoapp.backend.service;

/**
 * Signing and verification of authentication tokens (JWT). Interface kept separate from
 * the implementation (jjwt) so it can be mocked in unit tests.
 */
public interface TokenService {

    /** Generates a signed token carrying the user id. */
    String issueFor(long userId);

    /** Verifies a token's signature and returns the user id it carries. */
    long verify(String token);
}
