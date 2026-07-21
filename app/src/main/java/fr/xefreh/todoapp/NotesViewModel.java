package fr.xefreh.todoapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the notes list in memory for the list screen. The list is populated from the
 * server via {@code NotesRepository.fetchAll()} (see {@code NotesListFragment}) — there is
 * no local persistence; the ViewModel only survives configuration changes.
 */
public class NotesViewModel extends ViewModel {

    private final MutableLiveData<List<Note>> notes = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Note>> getNotes() {
        return notes;
    }

    /** Replaces the displayed list (must be called on the main thread). */
    public void setNotes(List<Note> newNotes) {
        notes.setValue(newNotes);
    }
}
