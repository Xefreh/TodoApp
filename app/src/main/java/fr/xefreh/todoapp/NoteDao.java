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

    /** Insère ou remplace une note (clé = id serveur). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Note note);

    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    LiveData<List<Note>> getAllLive();

    /** Liste synchrone (hors thread principal) pour les opérations de synchronisation. */
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    List<Note> getAll();

    @Delete
    void delete(Note note);

    /** Supprime une note par son id serveur. */
    @Query("DELETE FROM notes WHERE id = :id")
    void deleteById(long id);

    /** Vide le cache local (avant un rechargement complet depuis le serveur). */
    @Query("DELETE FROM notes")
    void clear();
}
