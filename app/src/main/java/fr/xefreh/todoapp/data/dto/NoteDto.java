package fr.xefreh.todoapp.data.dto;

/**
 * Representation of a note exchanged with the REST API.
 *
 * <p>On creation (POST), only {@code title}, {@code body}, {@code imageUri} are sent;
 * in the response, all fields are populated by the server ({@code id}, {@code createdAt}).</p>
 */
public class NoteDto {
    public Long id;
    public String title;
    public String body;
    public String imageUri;
    public Long createdAt;

    public NoteDto() {
    }

    public NoteDto(String title, String body, String imageUri) {
        this.title = title;
        this.body = body;
        this.imageUri = imageUri;
    }
}
