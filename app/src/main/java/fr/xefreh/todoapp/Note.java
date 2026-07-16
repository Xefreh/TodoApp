package fr.xefreh.todoapp;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {
    @PrimaryKey(autoGenerate = true)
    private Integer id;
    private final String title;
    private final String body;
    private final String imageUri;

    @Ignore
    public Note(String title, String body) {
        this(null, title, body, null);
    }

    @Ignore
    public Note(String title, String body, String imageUri) {
        this(null, title, body, imageUri);
    }

    @Ignore
    public Note(Integer id, String title, String body) {
        this(null, title, body, null);
    }

    public Note(Integer id, String title, String body, String imageUri) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.imageUri = imageUri;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() { return title; }
    public String getBody() { return body; }

    public String getImageUri() {
        return imageUri;
    }
}