package fr.xefreh.todoapp.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.xefreh.todoapp.backend.dto.NoteDto;
import fr.xefreh.todoapp.backend.service.AuthService;
import fr.xefreh.todoapp.backend.service.InvalidCredentialsException;
import fr.xefreh.todoapp.backend.service.InvalidTokenException;
import fr.xefreh.todoapp.backend.service.NoteNotFoundException;
import fr.xefreh.todoapp.backend.service.NoteService;
import fr.xefreh.todoapp.backend.service.TokenService;
import fr.xefreh.todoapp.backend.service.UsernameTakenException;
import fr.xefreh.todoapp.backend.service.WeakPasswordException;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import io.javalin.testtools.Request;
import io.javalin.testtools.Response;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * HTTP-level tests of the API error contract: the full Javalin stack (routes, AuthFilter,
 * exception handlers) runs on a random port via javalin-testtools, with the three services
 * mocked — no JPA, no database.
 *
 * Covers the paths that used to be validated manually with curl only: 400 on invalid
 * bodies and over-length fields, uniform {error, message} bodies, 401 from the filter,
 * 404 for unowned notes, 500 passthrough.
 */
class ApiErrorContractTest {

    private static final String GOOD_TOKEN = "good-token";
    private static final Consumer<Request.Builder> AUTH =
            req -> req.header("Authorization", "Bearer " + GOOD_TOKEN);

    private AuthService authService;
    private NoteService noteService;
    private TokenService tokenService;
    private Javalin app;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        noteService = mock(NoteService.class);
        tokenService = mock(TokenService.class);
        app = Main.createApp(authService, noteService, tokenService);
    }

    // --- 400 Bad Request ---

    @Test
    void register_withEmptyBody_returns400() {
        JavalinTest.test(app, (server, client) -> {
            Response res = client.post("/api/auth/register", "");
            assertError(res, 400, "BAD_REQUEST");
        });
    }

    @Test
    void register_withBlankUsername_returns400() {
        JavalinTest.test(app, (server, client) -> {
            Response res = client.post("/api/auth/register",
                    "{\"username\":\"  \",\"password\":\"whatever\"}");
            assertError(res, 400, "BAD_REQUEST");
        });
    }

    @Test
    void register_withTooLongUsername_returns400() {
        JavalinTest.test(app, (server, client) -> {
            Response res = client.post("/api/auth/register",
                    "{\"username\":\"" + "u".repeat(300) + "\",\"password\":\"whatever\"}");
            assertError(res, 400, "BAD_REQUEST");
        });
    }

    @Test
    void register_withShortPassword_returns400WeakPassword() {
        when(authService.register(anyString(), anyString()))
                .thenThrow(new WeakPasswordException(6));
        JavalinTest.test(app, (server, client) -> {
            Response res = client.post("/api/auth/register",
                    "{\"username\":\"carol\",\"password\":\"123\"}");
            assertError(res, 400, "WEAK_PASSWORD");
        });
    }

    @Test
    void register_withTakenUsername_returns409() {
        when(authService.register(anyString(), anyString()))
                .thenThrow(new UsernameTakenException("alice"));
        JavalinTest.test(app, (server, client) -> {
            Response res = client.post("/api/auth/register",
                    "{\"username\":\"alice\",\"password\":\"s3cret\"}");
            assertError(res, 409, "USERNAME_TAKEN");
        });
    }

    // --- 401 Unauthorized ---

    @Test
    void login_withInvalidCredentials_returns401() {
        when(authService.login(anyString(), anyString()))
                .thenThrow(new InvalidCredentialsException());
        JavalinTest.test(app, (server, client) -> {
            Response res = client.post("/api/auth/login",
                    "{\"username\":\"alice\",\"password\":\"wrong\"}");
            assertError(res, 401, "INVALID_CREDENTIALS");
        });
    }

    @Test
    void notes_withoutAuthorizationHeader_returns401() {
        JavalinTest.test(app, (server, client) -> {
            Response res = client.get("/api/notes");
            assertError(res, 401, "UNAUTHORIZED");
        });
    }

    @Test
    void notes_withInvalidToken_returns401() {
        when(tokenService.verify("bad-token")).thenThrow(new InvalidTokenException("bad token"));
        JavalinTest.test(app, (server, client) -> {
            Response res = client.get("/api/notes",
                    req -> req.header("Authorization", "Bearer bad-token"));
            assertError(res, 401, "UNAUTHORIZED");
        });
    }

    // --- 400 / 404 on notes ---

    @Test
    void getNote_withNonNumericId_returns400UniformBody() {
        when(tokenService.verify(GOOD_TOKEN)).thenReturn(1L);
        JavalinTest.test(app, (server, client) -> {
            Response res = client.get("/api/notes/abc", AUTH);
            assertError(res, 400, "BAD_REQUEST");
        });
    }

    @Test
    void getNote_whenNotOwned_returns404() {
        when(tokenService.verify(GOOD_TOKEN)).thenReturn(1L);
        when(noteService.getForOwner(5L, 1L)).thenThrow(new NoteNotFoundException(5L));
        JavalinTest.test(app, (server, client) -> {
            Response res = client.get("/api/notes/5", AUTH);
            assertError(res, 404, "NOT_FOUND");
        });
    }

    @Test
    void createNote_withBlankTitle_returns400() {
        when(tokenService.verify(GOOD_TOKEN)).thenReturn(1L);
        JavalinTest.test(app, (server, client) -> {
            Response res = client.post("/api/notes", "{\"title\":\"  \"}", AUTH);
            assertError(res, 400, "BAD_REQUEST");
        });
    }

    @Test
    void createNote_withTooLongTitle_returns400() {
        when(tokenService.verify(GOOD_TOKEN)).thenReturn(1L);
        JavalinTest.test(app, (server, client) -> {
            Response res = client.post("/api/notes",
                    "{\"title\":\"" + "x".repeat(300) + "\"}", AUTH);
            assertError(res, 400, "BAD_REQUEST");
        });
    }

    // --- happy path / 500 passthrough ---

    @Test
    void createNote_happyPath_returns201WithServerAssignedFields() {
        when(tokenService.verify(GOOD_TOKEN)).thenReturn(1L);
        when(noteService.create(eq(1L), any(NoteDto.class)))
                .thenReturn(new NoteDto(9L, "T", "B", null, 123L));
        JavalinTest.test(app, (server, client) -> {
            Response res = client.post("/api/notes", "{\"title\":\"T\",\"body\":\"B\"}", AUTH);
            assertEquals(201, res.code());
            String body = res.body().string();
            assertTrue(body.contains("\"id\":9"), body);
            assertTrue(body.contains("\"createdAt\":123"), body);
        });
    }

    @Test
    void unexpectedServiceError_returns500InternalError() {
        when(tokenService.verify(GOOD_TOKEN)).thenReturn(1L);
        when(noteService.listForOwner(1L)).thenThrow(new RuntimeException("boom"));
        JavalinTest.test(app, (server, client) -> {
            Response res = client.get("/api/notes", AUTH);
            assertError(res, 500, "INTERNAL_ERROR");
        });
    }

    // --- helper ---

    private static void assertError(Response res, int expectedStatus, String expectedError) {
        assertEquals(expectedStatus, res.code());
        String body = res.body().string();
        assertTrue(body.contains("\"error\":\"" + expectedError + "\""),
                "Expected error " + expectedError + " in body: " + body);
    }
}
