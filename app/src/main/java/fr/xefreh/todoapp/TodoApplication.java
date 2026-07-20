package fr.xefreh.todoapp;

import android.app.Application;

import fr.xefreh.todoapp.data.SessionManager;

/**
 * Application entry point. Initializes process-wide singletons (the Retrofit client).
 *
 * <p>Doing it here rather than in {@code LoginActivity} guarantees the API is available in
 * every process state — notably after a process death, where Android may restore
 * {@code MainActivity} directly without going through the launcher activity.</p>
 */
public class TodoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RetrofitProvider.init(new SessionManager(this));
    }
}
