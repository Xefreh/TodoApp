package fr.xefreh.todoapp.backend.dto;

/**
 * Représentation d'une note côté API.
 *
 * <p>En entrée (POST/PUT) seuls {@code title} et {@code body} (et {@code imageUri}) sont
 * attendus : {@code id} et {@code createdAt} sont assignés par le serveur. En sortie (GET)
 * tous les champs sont renseignés.</p>
 */
public class NoteDto {
    public Long id;
    public String title;
    public String body;
    public String imageUri;
    public Long createdAt;

    public NoteDto() {
    }

    public NoteDto(Long id, String title, String body, String imageUri, Long createdAt) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.imageUri = imageUri;
        this.createdAt = createdAt;
    }
}
