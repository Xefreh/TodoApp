package fr.xefreh.todoapp.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Persistent storage of the user session (JWT token, id, name).
 *
 * <p>Thin wrapper around {@link SharedPreferences} (private file {@code todo_session}).
 * Used by {@code AuthInterceptor} to retrieve the token to inject into each authenticated
 * request, and by {@code LoginActivity} to save/clear the session.</p>
 */
public class SessionManager {

    private static final String PREFS = "todo_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Saves the session after a successful login/register. */
    public void saveSession(String token, long userId, String username) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .apply();
    }

    /** The raw token, or {@code null} if not logged in. */
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    /** A ready-to-use {@code Authorization} HTTP header, or {@code null}. */
    public String authHeader() {
        String token = getToken();
        return token == null ? null : "Bearer " + token;
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1L);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    /** Clears the session (logout). */
    public void clear() {
        prefs.edit().clear().apply();
    }
}
