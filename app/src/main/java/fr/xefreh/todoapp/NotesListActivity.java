package fr.xefreh.todoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.xefreh.todoapp.data.ApiException;
import fr.xefreh.todoapp.data.NotesRepository;
import fr.xefreh.todoapp.data.NotesRepositoryImpl;
import fr.xefreh.todoapp.data.SessionManager;
import fr.xefreh.todoapp.ui.NotesListScreen;

public class NotesListActivity extends AppCompatActivity {

	private NotesListScreen screen;
	private NotesViewModel viewModel;
	private NotesRepository notesRepository;
	private SwipeNavigationDetector swipeNavigationDetector;
	private boolean isReturningToEditor;
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);

		notesRepository = new NotesRepositoryImpl(
				RetrofitProvider.getApi(),
				new SessionManager(this));

		screen = new NotesListScreen(this);
		setContentView(screen.root);
		swipeNavigationDetector = new SwipeNavigationDetector(
				this,
				SwipeNavigationDetector.Direction.RIGHT,
				this::returnToEditor);
		ViewCompat.setOnApplyWindowInsetsListener(screen.root, (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});
		screen.notesList.setLayoutManager(new LinearLayoutManager(this));

		screen.selectNotesNavigationItem();
		screen.bottomNavigation.setOnItemSelectedListener(item -> {
			if (screen.isCreateNavigationItem(item.getItemId())) {
				returnToEditor();
			}
			return false;
		});

		viewModel = new ViewModelProvider(this).get(NotesViewModel.class);
		viewModel.getNotes().observe(this, this::setNotesAdapter);

		screen.syncButton.setOnClickListener(v -> refreshNotes(true));
	}

	@Override
	protected void onResume() {
		super.onResume();
		// The server is the source of truth: reload the list every time the screen is shown
		// (first display, after creating a note, coming back to the screen).
		refreshNotes(false);
	}

	/**
	 * Fetches the notes from the server and publishes them to the ViewModel.
	 *
	 * @param manual true when triggered by the sync button (shows a success Toast); false
	 *               for automatic refreshes (silent on success).
	 */
	private void refreshNotes(boolean manual) {
		screen.syncButton.setEnabled(false);
		executorService.execute(() -> {
			try {
				List<Note> synced = notesRepository.fetchAll();
				runOnUiThread(() -> {
					viewModel.setNotes(synced);
					screen.syncButton.setEnabled(true);
					if (manual) {
						Toast.makeText(NotesListActivity.this,
								"Synced " + synced.size() + " notes",
								Toast.LENGTH_SHORT).show();
					}
				});
			} catch (ApiException e) {
				runOnUiThread(() -> {
					screen.syncButton.setEnabled(true);
					if (e.getHttpCode() == 401) {
						redirectToLogin();
						return;
					}
					Toast.makeText(NotesListActivity.this,
							"Sync failed: " + e.getMessage(),
							Toast.LENGTH_LONG).show();
				});
			}
		});
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (swipeNavigationDetector != null) {
			swipeNavigationDetector.onTouchEvent(event);
		}
		return super.dispatchTouchEvent(event);
	}

	private void returnToEditor() {
		if (isReturningToEditor) {
			return;
		}
		isReturningToEditor = true;
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
		finish();
	}

	/** The session expired or was revoked: back to the login screen, clearing the task. */
	private void redirectToLogin() {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
		finish();
	}

	private void setNotesAdapter(List<Note> notes) {
		NotesAdapter notesAdapter = new NotesAdapter(notes);
		screen.notesList.setAdapter(notesAdapter);
	}
}
