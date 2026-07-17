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
	private final long createdAt;

	@Ignore
	public Note(String title, String body) {
		this(null, title, body, null, System.currentTimeMillis());
	}

	@Ignore
	public Note(String title, String body, String imageUri) {
		this(null, title, body, imageUri, System.currentTimeMillis());
	}

	@Ignore
	public Note(Integer id, String title, String body) {
		this(id, title, body, null, System.currentTimeMillis());
	}

	public Note(Integer id, String title, String body, String imageUri, long createdAt) {
		this.id = id;
		this.title = title;
		this.body = body;
		this.imageUri = imageUri;
		this.createdAt = createdAt;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public String getBody() {
		return body;
	}

	public String getImageUri() {
		return imageUri;
	}

	public long getCreatedAt() {
		return createdAt;
	}
}
