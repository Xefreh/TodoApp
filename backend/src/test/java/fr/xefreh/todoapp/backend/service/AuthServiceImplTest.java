package fr.xefreh.todoapp.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.xefreh.todoapp.backend.model.UserEntity;
import fr.xefreh.todoapp.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link AuthServiceImpl}. All dependencies (interfaces) are mocked with
 * Mockito: we validate the orchestration logic without touching the database.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_persistsHashedUserAndReturnsToken() {
        // Arrange: the username is free, the hash and token are fixed values.
        when(userRepository.findByUsername("alice")).thenReturn(null);
        when(passwordHasher.hash("s3cret")).thenReturn("hashed-pw");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity u = inv.getArgument(0);
            u.setId(42L);
            return u;
        });
        when(tokenService.issueFor(42L)).thenReturn("jwt-42");

        // Act
        AuthResult result = authService.register("alice", "s3cret");

        // Assert
        assertEquals("jwt-42", result.token());
        assertEquals(42L, result.userId());
        verify(passwordHasher).hash("s3cret");
        verify(tokenService).issueFor(42L);
    }

    @Test
    void register_throwsWhenUsernameAlreadyTaken() {
        when(userRepository.findByUsername("alice")).thenReturn(new UserEntity("alice", "any-hash"));

        assertThrows(UsernameTakenException.class, () -> authService.register("alice", "s3cret"));

        // The password must not be hashed, nor the user saved.
        verify(passwordHasher, never()).hash(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void register_throwsWhenPasswordTooShort() {
        assertThrows(WeakPasswordException.class, () -> authService.register("alice", "12345"));

        // No lookup, no hash, no save: the policy rejects before any work.
        verify(userRepository, never()).findByUsername(anyString());
        verify(passwordHasher, never()).hash(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void register_acceptsPasswordOfExactlyMinLength() {
        when(userRepository.findByUsername("alice")).thenReturn(null);
        when(passwordHasher.hash("123456")).thenReturn("hashed-pw");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(tokenService.issueFor(1L)).thenReturn("jwt-1");

        // Boundary: exactly MIN_PASSWORD_LENGTH characters must be accepted.
        AuthResult result = authService.register("alice", "123456");

        assertEquals("jwt-1", result.token());
    }

    @Test
    void register_translatesConcurrentDuplicateIntoUsernameTaken() {
        // TOCTOU race: the username is free at check-time but taken by the time save runs
        // (unique constraint violation) -> the re-check finds it and yields a 409.
        when(userRepository.findByUsername("alice"))
                .thenReturn(null, new UserEntity("alice", "other-hash"));
        when(passwordHasher.hash("s3cret")).thenReturn("hashed-pw");
        when(userRepository.save(any(UserEntity.class)))
                .thenThrow(new RuntimeException("unique constraint violation"));

        assertThrows(UsernameTakenException.class, () -> authService.register("alice", "s3cret"));
    }

    @Test
    void register_rethrowsOriginalErrorWhenSaveFailsForOtherReasons() {
        // If the username is still absent after the save failure, it was NOT a duplicate
        // race: the original exception must propagate (e.g. database down).
        when(userRepository.findByUsername("alice")).thenReturn(null);
        when(passwordHasher.hash("s3cret")).thenReturn("hashed-pw");
        when(userRepository.save(any(UserEntity.class))).thenThrow(new RuntimeException("db down"));

        RuntimeException e = assertThrows(RuntimeException.class,
                () -> authService.register("alice", "s3cret"));
        assertEquals("db down", e.getMessage());
    }

    @Test
    void register_normalizesUsernameBeforeLookupAndPersist() {
        when(userRepository.findByUsername("alice")).thenReturn(null);
        when(passwordHasher.hash("s3cret")).thenReturn("hashed-pw");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity u = inv.getArgument(0);
            u.setId(3L);
            return u;
        });
        when(tokenService.issueFor(3L)).thenReturn("jwt-3");

        AuthResult result = authService.register("  Alice  ", "s3cret");

        verify(userRepository).findByUsername("alice");
        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(
                u -> "alice".equals(u.getUsername())));
        assertEquals("jwt-3", result.token());
    }

    @Test
    void login_normalizesUsernameBeforeLookup() {
        UserEntity stored = new UserEntity("alice", "hashed-pw");
        stored.setId(7L);
        when(userRepository.findByUsername("alice")).thenReturn(stored);
        when(passwordHasher.verify("s3cret", "hashed-pw")).thenReturn(true);
        when(tokenService.issueFor(7L)).thenReturn("jwt-7");

        AuthResult result = authService.login("ALICE ", "s3cret");

        assertEquals("jwt-7", result.token());
        verify(userRepository).findByUsername("alice");
    }

    @Test
    void login_succeedsWhenPasswordMatches() {
        UserEntity stored = new UserEntity("alice", "hashed-pw");
        stored.setId(7L);
        when(userRepository.findByUsername("alice")).thenReturn(stored);
        when(passwordHasher.verify("s3cret", "hashed-pw")).thenReturn(true);
        when(tokenService.issueFor(7L)).thenReturn("jwt-7");

        AuthResult result = authService.login("alice", "s3cret");

        assertEquals("jwt-7", result.token());
        assertEquals(7L, result.userId());
    }

    @Test
    void login_failsWhenUserUnknown() {
        when(userRepository.findByUsername("nobody")).thenReturn(null);
        when(passwordHasher.dummyHash()).thenReturn("dummy-hash");
        when(passwordHasher.verify("x", "dummy-hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login("nobody", "x"));

        // A full verification still runs against the dummy hash (constant-time login).
        verify(passwordHasher).verify("x", "dummy-hash");
        verify(tokenService, never()).issueFor(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void login_failsWhenPasswordMismatch() {
        UserEntity stored = new UserEntity("alice", "hashed-pw");
        stored.setId(7L);
        when(userRepository.findByUsername("alice")).thenReturn(stored);
        when(passwordHasher.verify("wrong", "hashed-pw")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login("alice", "wrong"));

        verify(tokenService, never()).issueFor(org.mockito.ArgumentMatchers.anyLong());
    }
}
