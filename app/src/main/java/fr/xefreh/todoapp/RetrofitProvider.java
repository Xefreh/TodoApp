package fr.xefreh.todoapp;

import fr.xefreh.todoapp.data.SessionManager;
import fr.xefreh.todoapp.data.TodoApi;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Fournit un singleton {@link TodoApi} (Retrofit) pointant vers le backend local.
 *
 * <p>{@code baseUrl} utilise {@code 10.0.2.2}, qui depuis l'émulateur Android correspond au
 * {@code localhost} de la machine hôte (où tourne le serveur Javalin sur le port 7000).
 * Le trafic est en HTTP clair : autorisé via {@code network_security_config.xml} pour ce
 * domaine uniquement.</p>
 *
 * <p>Un {@code AuthInterceptor} injecte l'en-tête {@code Authorization: Bearer <token>} lu
 * dans le {@link SessionManager} sur chaque requête authentifiée.</p>
 */
public final class RetrofitProvider {

    /** Base URL du backend, joignable depuis l'émulateur via l'alias réseau hôte. */
    public static final String BASE_URL = "http://10.0.2.2:7000/api/";

    private static volatile TodoApi api;

    private RetrofitProvider() {
    }

    /** Initialise le singleton avec le {@link SessionManager} de l'application. À appeler au démarrage. */
    public static void init(SessionManager sessionManager) {
        if (api == null) {
            synchronized (RetrofitProvider.class) {
                if (api == null) {
                    api = build(sessionManager);
                }
            }
        }
    }

    /** Récupère l'API. Doit être appelé après {@link #init(SessionManager)}. */
    public static TodoApi getApi() {
        if (api == null) {
            throw new IllegalStateException("RetrofitProvider not initialized. Call init(SessionManager) first.");
        }
        return api;
    }

    private static TodoApi build(SessionManager sessionManager) {
        Interceptor authInterceptor = chain -> {
            okhttp3.Request.Builder builder = chain.request().newBuilder();
            String authHeader = sessionManager.authHeader();
            if (authHeader != null && chain.request().header("Authorization") == null) {
                builder.addHeader("Authorization", authHeader);
            }
            Response response = chain.proceed(builder.build());
            // 401 côté serveur => session invalide (token expiré/révoqué).
            if (response.code() == 401) {
                sessionManager.clear();
            }
            return response;
        };

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(TodoApi.class);
    }
}
