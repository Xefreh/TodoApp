package fr.xefreh.todoapp.data.dto;

/**
 * Représentation d'une note échangée avec l'API REST.
 *
 * <p>En création (POST), seuls {@code title}, {@code body}, {@code imageUri} sont envoyés ;
 * en réponse, tous les champs sont renseignés par le serveur ({@code id}, {@code createdAt}).</p>
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
