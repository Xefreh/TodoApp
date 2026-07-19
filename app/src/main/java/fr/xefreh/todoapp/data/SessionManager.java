package fr.xefreh.todoapp.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Stockage persistant de la session utilisateur (jeton JWT, id, nom).
 *
 * <p>Wrapper fin autour de {@link SharedPreferences} (fichier privé {@code todo_session}).
 * Utilisé par {@code AuthInterceptor} pour récupérer le jeton à injecter dans chaque requête
 * authentifiée, et par {@code LoginActivity} pour mémoriser/effacer la session.</p>
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

    /** Mémorise la session après un login/register réussi. */
    public void saveSession(String token, long userId, String username) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .apply();
    }

    /** Le jeton brut, ou {@code null} si non connecté. */
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    /** L'en-tête HTTP {@code Authorization} prêt à l'emploi, ou {@code null}. */
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

    /** Efface la session (déconnexion). */
    public void clear() {
        prefs.edit().clear().apply();
    }
}
