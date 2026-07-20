package fr.xefreh.todoapp.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link JwtTokenService} on the real implementation (no mock): we validate the
 * sign→verify roundtrip and the detection of malformed/tampered tokens.
 *
 * <p>The test key is explicit (>= 32 bytes) via the dedicated constructor.</p>
 */
class JwtTokenServiceTest {

    private static final String TEST_SECRET = "test-secret-with-at-least-32-bytes!!";

    private final JwtTokenService tokenService = new JwtTokenService(TEST_SECRET);

    @Test
    void verify_returnsUserIdForValidToken() {
        String token = tokenService.issueFor(123L);

        long userId = tokenService.verify(token);

        assertEquals(123L, userId);
    }

    @Test
    void verify_throwsForGarbageToken() {
        assertThrows(InvalidTokenException.class, () -> tokenService.verify("not-a-jwt"));
    }

    @Test
    void verify_throwsForTokenSignedWithDifferentKey() {
        // Token signed by another service (different key).
        JwtTokenService other = new JwtTokenService("another-32-byte-secret-key-here!!!");
        String foreignToken = other.issueFor(1L);

        assertThrows(InvalidTokenException.class, () -> tokenService.verify(foreignToken));
    }

    @Test
    void verify_throwsForTamperedToken() {
        String token = tokenService.issueFor(1L);
        String tampered = token.substring(0, token.length() - 3) + "AAA";

        assertThrows(InvalidTokenException.class, () -> tokenService.verify(tampered));
    }

    @Test
    void issuedTokensAreDifferentAcrossUsers() {
        String t1 = tokenService.issueFor(1L);
        String t2 = tokenService.issueFor(2L);

        assertNotEquals(t1, t2);
    }
}
