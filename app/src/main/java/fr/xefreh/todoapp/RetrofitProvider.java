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
 * Provides a singleton {@link TodoApi} (Retrofit) pointing to the local backend.
 *
 * <p>{@code baseUrl} uses {@code 10.0.2.2}, which from the Android emulator maps to the
 * host machine's {@code localhost} (where the Javalin server runs on port 7000).
 * Traffic is plain HTTP: allowed via {@code network_security_config.xml} for this
 * domain only.</p>
 *
 * <p>An {@code AuthInterceptor} injects the {@code Authorization: Bearer <token>} header,
 * read from the {@link SessionManager}, on every authenticated request.</p>
 */
public final class RetrofitProvider {

    /** Base URL of the backend, reachable from the emulator via the host network alias. */
    public static final String BASE_URL = "http://10.0.2.2:7000/api/";

    private static volatile TodoApi api;

    private RetrofitProvider() {
    }

    /** Initializes the singleton with the application's {@link SessionManager}. Call at startup. */
    public static void init(SessionManager sessionManager) {
        if (api == null) {
            synchronized (RetrofitProvider.class) {
                if (api == null) {
                    api = build(sessionManager);
                }
            }
        }
    }

    /** Returns the API. Must be called after {@link #init(SessionManager)}. */
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
            // 401 from the server => invalid session (expired/revoked token).
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
