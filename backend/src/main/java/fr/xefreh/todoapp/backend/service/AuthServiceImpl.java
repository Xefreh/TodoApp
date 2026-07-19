package fr.xefreh.todoapp.backend.service;

import fr.xefreh.todoapp.backend.model.UserEntity;
import fr.xefreh.todoapp.backend.repository.UserRepository;

/**
 * Implémentation de {@link AuthService} construite par injection de dépendances.
 *
 * Toutes les dépendances sont des interfaces : la classe est donc entièrement testable
 * avec des mocks Mockito (cf. AuthServiceTest).
 */
public class AuthServiceImpl implements AuthService {

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
        if (userRepository.findByUsername(username) != null) {
            throw new UsernameTakenException(username);
        }
        String hash = passwordHasher.hash(password);
        UserEntity saved = userRepository.save(new UserEntity(username, hash));
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
