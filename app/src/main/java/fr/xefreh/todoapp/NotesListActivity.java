package fr.xefreh.todoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.xefreh.todoapp.ui.NotesListScreen;

public class NotesListActivity extends AppCompatActivity {

	private AppDatabase appDatabase;
	private SwipeNavigationDetector swipeNavigationDetector;
	private boolean isReturningToEditor;
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		appDatabase = DatabaseProvider.getDatabase(this);

		NotesListScreen screen = new NotesListScreen(this);
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


		NotesViewModel viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
			@NonNull
			@Override
			public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
				return (T) new NotesViewModel(appDatabase);
			}
		}).get(NotesViewModel.class);

		viewModel.getNotes().observe(this, notes -> setNotesAdapter(notes, screen));

		screen.syncButton.setOnClickListener((v -> {
			screen.syncButton.setEnabled(false);
			executorService.execute(() -> {
				fr.xefreh.todoapp.data.NotesRepository repo =
						new fr.xefreh.todoapp.data.NotesRepositoryImpl(
								RetrofitProvider.getApi(),
								appDatabase.noteDao(),
								new fr.xefreh.todoapp.data.SessionManager(this));
				try {
					java.util.List<Note> synced = repo.fetchAll();
					runOnUiThread(() -> {
						screen.syncButton.setEnabled(true);
						Toast.makeText(NotesListActivity.this,
								"Synced " + synced.size() + " notes",
								Toast.LENGTH_SHORT).show();
					});
				} catch (fr.xefreh.todoapp.data.ApiException e) {
					runOnUiThread(() -> {
						screen.syncButton.setEnabled(true);
						Toast.makeText(NotesListActivity.this,
								"Sync failed: " + e.getMessage(),
								Toast.LENGTH_LONG).show();
					});
				}
			});
		}));
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

	private static void setNotesAdapter(List<Note> notes, NotesListScreen screen) {
		NotesAdapter notesAdapter = new NotesAdapter(notes);
		screen.notesList.setAdapter(notesAdapter);
	}
}
