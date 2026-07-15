package fr.xefreh.todoapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class NotesViewModel extends ViewModel {
    private final AppDatabase appDatabase;

    public NotesViewModel(AppDatabase appDatabase) {
        this.appDatabase = appDatabase;
    }

    public LiveData<List<Note>> getNotes() {
        return appDatabase.noteDao().getAllLive();
    }
}