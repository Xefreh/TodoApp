package fr.xefreh.todoapp.data;

import fr.xefreh.todoapp.data.dto.AuthResponse;
import fr.xefreh.todoapp.data.dto.Credentials;
import fr.xefreh.todoapp.data.dto.NoteDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Interface du client REST (Retrofit) vers le backend Javalin.
 *
 * <p>Les routes sous {@code /notes} requièrent un en-tête {@code Authorization: Bearer <token>}.
 * Celui-ci est normalement injecté automatiquement par {@code AuthInterceptor} (OkHttp), mais
 * les méthodes l'acceptent aussi explicitement en paramètre pour permettre les tests sans
 * intercepteur.</p>
 */
public interface TodoApi {

    // --- Authentification (publique) ---

    @POST("auth/register")
    Call<AuthResponse> register(@Body Credentials credentials);

    @POST("auth/login")
    Call<AuthResponse> login(@Body Credentials credentials);

    // --- Notes (authentifiées) ---

    @GET("notes")
    Call<List<NoteDto>> getNotes(@Header("Authorization") String authHeader);

    @GET("notes/{id}")
    Call<NoteDto> getNote(@Header("Authorization") String authHeader, @Path("id") long id);

    @POST("notes")
    Call<NoteDto> createNote(@Header("Authorization") String authHeader, @Body NoteDto note);

    @PUT("notes/{id}")
    Call<NoteDto> updateNote(@Header("Authorization") String authHeader,
                             @Path("id") long id,
                             @Body NoteDto note);

    @DELETE("notes/{id}")
    Call<Void> deleteNote(@Header("Authorization") String authHeader, @Path("id") long id);
}
