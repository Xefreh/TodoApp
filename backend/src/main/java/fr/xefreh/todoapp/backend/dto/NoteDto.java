package fr.xefreh.todoapp.backend.dto;

/**
 * API-side representation of a note.
 *
 * <p>On input (POST/PUT) only {@code title} and {@code body} (and {@code imageUri}) are
 * expected: {@code id} and {@code createdAt} are assigned by the server. On output (GET)
 * all fields are populated.</p>
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
