package fr.xefreh.todoapp.backend.service;

import fr.xefreh.todoapp.backend.model.UserEntity;
import fr.xefreh.todoapp.backend.repository.UserRepository;

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
        if (userRepository.findByUsername(username) != null) {
            throw new UsernameTakenException(username);
        }
        String hash = passwordHasher.hash(password);
        UserEntity saved;
        try {
            saved = userRepository.save(new UserEntity(username, hash));
        } catch (RuntimeException e) {
            // TOCTOU race: a concurrent request inserted the same username between the check
            // above and this insert, and the unique constraint rejected it. If the username
            // now exists, translate the failure into the expected 409; otherwise rethrow.
            if (userRepository.findByUsername(username) != null) {
                throw new UsernameTakenException(username);
            }
            throw e;
        }
        String token = tokenService.issueFor(saved.getId());
        return new AuthResult(token, saved.getId());
    }

    @Override
    public AuthResult login(String username, String password) {
        UserEntity user = userRepository.findByUsername(username);
        if (user == null || !passwordHasher.verify(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        String token = tokenService.issueFor(user.getId());
        return new AuthResult(token, user.getId());
    }
}
