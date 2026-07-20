package fr.xefreh.todoapp;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Note persisted locally as a cache of the REST API.
 *
 * <p>Since the switch to the REST API, {@code id} is the **server** identifier (no more
 * local auto-generation): notes are created on the server, then inserted locally with the
 * received id. {@code createdAt} is still set by the server.</p>
 *
 * <p>{@code title}, {@code body}, {@code imageUri} are now mutable to allow local updates
 * after a server PUT.</p>
 */
@Entity(tableName = "notes")
public class Note {
    @PrimaryKey
    private Long id;
    private String title;
    private String body;
    private String imageUri;
    private long createdAt;

    /** Required Room constructor. */
    public Note(Long id, String title, String body, String imageUri, long createdAt) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.imageUri = imageUri;
        this.createdAt = createdAt;
    }

    /** Builds a local note ready to be sent to the server (id and createdAt assigned by the API). */
    @Ignore
    public Note(String title, String body, String imageUri) {
        this(null, title, body, imageUri, 0L);
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
