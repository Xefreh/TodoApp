package fr.xefreh.todoapp;

/**
 * Note model.
 *
 * <p>Since the switch to the REST API, {@code id} is the <b>server</b> identifier: notes are
 * created on the server, which assigns the id and {@code createdAt}. The server is the only
 * source of truth — there is no local persistence.</p>
 *
 * <p>{@code title}, {@code body}, {@code imageUri} are mutable to allow updates (PUT).</p>
 */
public class Note {
    private Long id;
    private String title;
    private String body;
    private String imageUri;
    private long createdAt;

    public Note(Long id, String title, String body, String imageUri, long createdAt) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.imageUri = imageUri;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
