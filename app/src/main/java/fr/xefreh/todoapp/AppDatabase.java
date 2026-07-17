package fr.xefreh.todoapp;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Note.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {
	public abstract NoteDao noteDao();
}