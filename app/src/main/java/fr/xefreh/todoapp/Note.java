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

    @Ignore
    public Note(String title, String body) {
        this(null, title, body);
    }

    public Note(Integer id, String title, String body) {
        this.id = id;
        this.title = title;
        this.body = body;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() { return title; }
    public String getBody() { return body; }
}