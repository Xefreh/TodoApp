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
 * REST client interface (Retrofit) for the Javalin backend.
 *
 * <p>Routes under {@code /notes} require an {@code Authorization: Bearer <token>} header.
 * It is normally injected automatically by {@code AuthInterceptor} (OkHttp), but the methods
 * also accept it explicitly as a parameter to allow testing without an interceptor.</p>
 */
public interface TodoApi {

    // --- Authentication (public) ---

    @POST("auth/register")
    Call<AuthResponse> register(@Body Credentials credentials);

    @POST("auth/login")
    Call<AuthResponse> login(@Body Credentials credentials);

    // --- Notes (authenticated) ---

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
