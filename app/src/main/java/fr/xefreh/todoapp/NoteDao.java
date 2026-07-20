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

    /** Inserts or replaces a note (key = server id). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Note note);

    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    LiveData<List<Note>> getAllLive();

    /** Synchronous list (off the main thread) for synchronization operations. */
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    List<Note> getAll();

    @Delete
    void delete(Note note);

    /** Deletes a note by its server id. */
    @Query("DELETE FROM notes WHERE id = :id")
    void deleteById(long id);

    /** Clears the local cache (before a full reload from the server). */
    @Query("DELETE FROM notes")
    void clear();
}
