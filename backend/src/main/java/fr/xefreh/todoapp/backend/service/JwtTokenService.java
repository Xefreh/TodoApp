package fr.xefreh.todoapp.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import javax.crypto.SecretKey;

/**
 * {@link TokenService} implementation based on jjwt.
 *
 * The secret key is read from the system property or environment variable {@code TODO_JWT_SECRET}.
 * If no key of at least 32 bytes is provided, a development key is derived deterministically —
 * sufficient for the local prototype but must be replaced in production.
 *
 * Tokens expire after {@link #TOKEN_TTL} (24 h).
 */
public class JwtTokenService implements TokenService {

    private static final Duration TOKEN_TTL = Duration.ofHours(24);
    private static final String SUBJECT_CLAIM = "uid";

    private final SecretKey key;

    public JwtTokenService() {
        this(resolveSecret());
    }

    /** Test constructor allowing an explicit key to be injected. */
    public JwtTokenService(String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String issueFor(long userId) {
        Date now = new Date();
        return Jwts.builder()
                .claim(SUBJECT_CLAIM, userId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + TOKEN_TTL.toMillis()))
                .signWith(key)
                .compact();
    }

    @Override
    public long verify(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Object uid = claims.get(SUBJECT_CLAIM);
            if (uid instanceof Number n) {
                return n.longValue();
            }
            throw new InvalidTokenException("Token missing user id claim");
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid or expired token");
        }
    }

    private static String resolveSecret() {
        String secret = System.getenv("TODO_JWT_SECRET");
        if (secret == null) {
            secret = System.getProperty("TODO_JWT_SECRET");
        }
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            // Development key — 32+ bytes, stable within a session.
            return "todobackend-dev-secret-do-not-use-in-prod!!";
        }
        return secret;
    }
}
