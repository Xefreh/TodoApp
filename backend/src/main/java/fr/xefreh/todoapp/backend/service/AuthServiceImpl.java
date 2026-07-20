package fr.xefreh.todoapp.backend.service;

import fr.xefreh.todoapp.backend.model.UserEntity;
import fr.xefreh.todoapp.backend.repository.UserRepository;
import java.util.Locale;

/**
 * {@link AuthService} implementation built via dependency injection.
 *
 * All dependencies are interfaces: the class is therefore fully testable with Mockito
 * mocks (see AuthServiceTest).
 */
public class AuthServiceImpl implements AuthService {

    /** Minimum password length enforced on registration (matches the Android client rule). */
    static final int MIN_PASSWORD_LENGTH = 6;

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordHasher passwordHasher,
                           TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
    }

    @Override
    public AuthResult register(String username, String password) {
        // Server-side policy: never rely on client validation alone.
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new WeakPasswordException(MIN_PASSWORD_LENGTH);
        }
        String normalized = normalizeUsername(username);
        if (userRepository.findByUsername(normalized) != null) {
            throw new UsernameTakenException(normalized);
        }
        String hash = passwordHasher.hash(password);
        UserEntity saved;
        try {
            saved = userRepository.save(new UserEntity(normalized, hash));
        } catch (RuntimeException e) {
            // TOCTOU race: a concurrent request inserted the same username between the check
            // above and this insert, and the unique constraint rejected it. If the username
            // now exists, translate the failure into the expected 409; otherwise rethrow.
            if (userRepository.findByUsername(normalized) != null) {
                throw new UsernameTakenException(normalized);
            }
            throw e;
        }
        String token = tokenService.issueFor(saved.getId());
        return new AuthResult(token, saved.getId());
    }

    @Override
    public AuthResult login(String username, String password) {
        UserEntity user = userRepository.findByUsername(normalizeUsername(username));
        // Constant-time behavior: run a full argon2 verification even when the user does
        // not exist, so the response time does not reveal whether the account exists.
        // Beware: the verification must be evaluated BEFORE checking user == null —
        // "user == null || !verify(...)" would short-circuit and defeat the dummy hash.
        String hash = user != null ? user.getPasswordHash() : passwordHasher.dummyHash();
        boolean passwordMatches = passwordHasher.verify(password, hash);
        if (user == null || !passwordMatches) {
            throw new InvalidCredentialsException();
        }
        String token = tokenService.issueFor(user.getId());
        return new AuthResult(token, user.getId());
    }

    /**
     * Canonical form of a username: trimmed and lower-cased. Applied on register and login
     * so that "Alice", "alice " and "ALICE" designate the same account (H2 compares
     * case-sensitively, which would otherwise allow confusing duplicates).
     */
    private static String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
