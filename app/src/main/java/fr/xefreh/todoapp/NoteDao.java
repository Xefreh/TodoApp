package fr.xefreh.todoapp;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NoteDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(Note note);

	@Query("SELECT * FROM notes ORDER BY createdAt DESC")
	LiveData<List<Note>> getAllLive();

	@Delete
	void delete(Note note);
}