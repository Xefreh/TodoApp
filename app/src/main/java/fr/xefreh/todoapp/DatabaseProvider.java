package fr.xefreh.todoapp;

import android.content.Context;

import androidx.room.Room;

public class DatabaseProvider {
	private static AppDatabase instance;

	public static AppDatabase getDatabase(Context context) {
		if (instance == null) {
			instance = Room.databaseBuilder(
					context.getApplicationContext(),
					AppDatabase.class,
					"notes-db"
			).fallbackToDestructiveMigration().build();
		}
		return instance;
	}
}