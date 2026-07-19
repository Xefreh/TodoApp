package fr.xefreh.todoapp;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Note persistée localement en cache de l'API REST.
 *
 * <p>Depuis la bascule vers l'API REST, l'{@code id} est l'identifiant **serveur** (plus
 * d'auto-génération locale) : les notes sont créées côté serveur puis insérées localement
 * avec l'id reçu. {@code createdAt} reste fixé par le serveur.</p>
 *
 * <p>{@code title}, {@code body}, {@code imageUri} sont désormais mutables pour permettre
 * la mise à jour locale après un PUT serveur.</p>
 */
@Entity(tableName = "notes")
public class Note {
    @PrimaryKey
    private Long id;
    private String title;
    private String body;
    private String imageUri;
    private long createdAt;

    /** Constructeur Room requis. */
    public Note(Long id, String title, String body, String imageUri, long createdAt) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.imageUri = imageUri;
        this.createdAt = createdAt;
    }

    /** Construit une note locale prête à être envoyée au serveur (id et createdAt assignés par l'API). */
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
