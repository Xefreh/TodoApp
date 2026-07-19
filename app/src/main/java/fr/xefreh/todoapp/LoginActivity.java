package fr.xefreh.todoapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import fr.xefreh.todoapp.data.SessionManager;
import fr.xefreh.todoapp.data.TodoApi;
import fr.xefreh.todoapp.data.dto.AuthResponse;
import fr.xefreh.todoapp.data.dto.Credentials;
import fr.xefreh.todoapp.ui.AuthScreen;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Écran de démarrage (launcher) : authentifie l'utilisateur avant d'accéder aux notes.
 *
 * <p>Si une session valide existe déjà (token mémorisé), redirige directement vers
 * {@link MainActivity}. Sinon affiche l'{@link AuthScreen} et, sur login/register réussi,
 * sauvegarde la session ({@link SessionManager}) puis démarre {@code MainActivity}.</p>
 */
public class LoginActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private AuthScreen screen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        sessionManager = new SessionManager(this);
        RetrofitProvider.init(sessionManager);

        // Déjà connecté -> on court-circuite l'écran de login.
        if (sessionManager.isLoggedIn()) {
            startMainActivity();
            return;
        }

        screen = new AuthScreen(this);
        setContentView(screen.root);
        ViewCompat.setOnApplyWindowInsetsListener(screen.root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        screen.switchModeButton.setOnClickListener(v ->
                screen.applyMode(screen.getMode() == AuthScreen.Mode.LOGIN
                        ? AuthScreen.Mode.REGISTER
                        : AuthScreen.Mode.LOGIN));

        screen.submitButton.setOnClickListener(v -> submit());
    }

    private void submit() {
        String username = textOf(screen.usernameInput);
        String password = textOf(screen.passwordInput);

        if (username.isEmpty()) {
            screen.usernameLayout.setError(getString(R.string.error_username_required));
            return;
        }
        screen.usernameLayout.setError(null);
        if (password.isEmpty()) {
            screen.passwordLayout.setError(getString(R.string.error_password_required));
            return;
        }
        if (password.length() < 6) {
            screen.passwordLayout.setError(getString(R.string.error_password_too_short));
            return;
        }
        screen.passwordLayout.setError(null);

        boolean register = screen.getMode() == AuthScreen.Mode.REGISTER;
        if (register) {
            String confirm = textOf(screen.passwordConfirmInput);
            if (!confirm.equals(password)) {
                screen.passwordConfirmLayout.setError(getString(R.string.error_password_mismatch));
                return;
            }
            screen.passwordConfirmLayout.setError(null);
        }

        screen.submitButton.setEnabled(false);
        Toast.makeText(this, R.string.auth_in_progress, Toast.LENGTH_SHORT).show();

        TodoApi api = RetrofitProvider.getApi();
        Credentials credentials = new Credentials(username, password);
        Call<AuthResponse> call = register ? api.register(credentials) : api.login(credentials);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call,
                                   @NonNull Response<AuthResponse> response) {
                screen.submitButton.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse body = response.body();
                    sessionManager.saveSession(body.token, body.userId, username);
                    Toast.makeText(LoginActivity.this,
                            register ? R.string.auth_success_register : R.string.auth_success_login,
                            Toast.LENGTH_SHORT).show();
                    startMainActivity();
                } else if (response.code() == 401 || response.code() == 409) {
                    Toast.makeText(LoginActivity.this,
                            register ? R.string.auth_failed_register : R.string.auth_failed_login,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, R.string.auth_error_generic, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                screen.submitButton.setEnabled(true);
                Toast.makeText(LoginActivity.this,
                        getString(R.string.auth_error_network, t.getMessage()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private static String textOf(android.widget.EditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }
}
