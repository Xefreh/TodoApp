package fr.xefreh.todoapp;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.xefreh.todoapp.data.ApiException;
import fr.xefreh.todoapp.data.NotesRepository;
import fr.xefreh.todoapp.data.NotesRepositoryImpl;
import fr.xefreh.todoapp.data.SessionManager;
import fr.xefreh.todoapp.ui.NotesListScreen;

public class NotesListFragment extends Fragment implements MainActivity.SwipeTarget {
	private NotesListScreen screen;
	private NotesViewModel viewModel;
	private NotesRepository notesRepository;
	private SwipeNavigationDetector swipeNavigationDetector;
	private ExecutorService executorService;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		if (!new SessionManager(requireContext()).isLoggedIn()) {
			((MainActivity) requireActivity()).redirectToLogin();
			return new View(requireContext());
		}
		notesRepository = new NotesRepositoryImpl(RetrofitProvider.getApi(),
				new SessionManager(requireContext()));
		executorService = Executors.newSingleThreadExecutor();
		screen = new NotesListScreen(requireContext());
		screen.notesList.setLayoutManager(new LinearLayoutManager(requireContext()));
		swipeNavigationDetector = new SwipeNavigationDetector(requireContext(),
				SwipeNavigationDetector.Direction.RIGHT,
				() -> ((MainActivity) requireActivity()).showTab(0));
		viewModel = new ViewModelProvider(this).get(NotesViewModel.class);
		viewModel.getNotes().observe(this, this::setNotesAdapter);
		screen.syncButton.setOnClickListener(v -> refreshNotes(true));
		return screen.root;
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshNotes(false);
	}

	private void refreshNotes(boolean manual) {
		if (screen == null || executorService == null) return;
		screen.syncButton.setEnabled(false);
		executorService.execute(() -> {
			try {
				List<Note> synced = notesRepository.fetchAll();
				requireActivity().runOnUiThread(() -> {
					if (screen == null) return;
					viewModel.setNotes(synced);
					screen.syncButton.setEnabled(true);
					if (manual) Toast.makeText(requireContext(),
							getString(R.string.synced_notes_count, synced.size()), Toast.LENGTH_SHORT).show();
				});
			} catch (Exception e) {
				requireActivity().runOnUiThread(() -> {
					if (screen == null) return;
					screen.syncButton.setEnabled(true);
					if (e instanceof ApiException apiException && apiException.getHttpCode() == 401) {
						((MainActivity) requireActivity()).redirectToLogin();
						return;
					}
					Toast.makeText(requireContext(), getString(R.string.sync_failed, e.getMessage()),
							Toast.LENGTH_LONG).show();
				});
			}
		});
	}

	@Override
	public void onTouchEvent(MotionEvent event) {
		if (swipeNavigationDetector != null) swipeNavigationDetector.onTouchEvent(event);
	}

	@Override
	public void onDestroyView() {
		if (executorService != null) executorService.shutdown();
		screen = null;
		super.onDestroyView();
	}

	private void setNotesAdapter(List<Note> notes) {
		if (screen == null) return;
		RecyclerView.LayoutManager layoutManager = screen.notesList.getLayoutManager();
		Parcelable scrollState = layoutManager == null ? null : layoutManager.onSaveInstanceState();
		screen.notesList.setAdapter(new NotesAdapter(notes));
		if (layoutManager != null && scrollState != null) {
			layoutManager.onRestoreInstanceState(scrollState);
		}
	}
}
