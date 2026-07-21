package fr.xefreh.todoapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.xefreh.todoapp.data.ApiException;
import fr.xefreh.todoapp.data.NotesRepository;
import fr.xefreh.todoapp.data.NotesRepositoryImpl;
import fr.xefreh.todoapp.data.SessionManager;
import fr.xefreh.todoapp.ui.MainScreen;

public class CreateNoteFragment extends Fragment implements MainActivity.SwipeTarget {
	private static final String STATE_PHOTO_PATH = "state_photo_path";

	private MainScreen screen;
	private File photoFile;
	private Uri photoUri;
	private SwipeNavigationDetector swipeNavigationDetector;
	private ExecutorService executorService;
	private NotesRepository notesRepository;

	private final ActivityResultLauncher<Uri> takePhotoLauncher = registerForActivityResult(
			new ActivityResultContracts.TakePicture(), wasSaved -> {
				if (screen == null) return;
				if (wasSaved) {
					screen.photoCard.setVisibility(View.VISIBLE);
					Glide.with(this).load(photoUri).centerCrop().into(screen.photoPreview);
					Toast.makeText(requireContext(), getString(R.string.photo_saved_to, photoFile.getPath()), Toast.LENGTH_LONG).show();
				} else Toast.makeText(requireContext(), R.string.photo_save_failed, Toast.LENGTH_SHORT).show();
			});

	private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
			new ActivityResultContracts.RequestPermission(), isGranted -> {
				if (isGranted) {
					launchCamera();
				} else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
					new AlertDialog.Builder(requireContext())
							.setTitle(R.string.camera_access_needed_title)
							.setMessage(R.string.camera_access_rationale)
							.setPositiveButton(R.string.action_try_again, (dialog, which) ->
									retryCameraPermission())
							.setNegativeButton(R.string.action_cancel, null).show();
				} else Toast.makeText(requireContext(), R.string.camera_permission_disabled, Toast.LENGTH_SHORT).show();
			});

	private final ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher = registerForActivityResult(
			new ActivityResultContracts.PickVisualMedia(), uri -> {
				if (uri != null) importPickedImage(uri);
			});

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		if (!new SessionManager(requireContext()).isLoggedIn()) {
			((MainActivity) requireActivity()).redirectToLogin();
			return new View(requireContext());
		}
		notesRepository = new NotesRepositoryImpl(RetrofitProvider.getApi(), new SessionManager(requireContext()));
		executorService = Executors.newSingleThreadExecutor();
		screen = new MainScreen(requireContext());
		if (savedInstanceState != null) restorePhoto(savedInstanceState.getString(STATE_PHOTO_PATH));
		swipeNavigationDetector = new SwipeNavigationDetector(requireContext(),
				SwipeNavigationDetector.Direction.LEFT,
				() -> ((MainActivity) requireActivity()).showTab(1));

		screen.titleInput.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override public void afterTextChanged(Editable s) {
				if (!s.toString().trim().isEmpty()) screen.titleInputLayout.setError(null);
			}
		});
		screen.saveButton.setOnClickListener(v -> saveNote());
		screen.attachPhotoButton.setOnClickListener(v -> showPhotoChooser());
		return screen.root;
	}

	private void saveNote() {
		String title = screen.titleInput.getText().toString().trim();
		String body = screen.bodyInput.getText().toString();
		if (title.isEmpty()) {
			screen.titleInputLayout.setError(getString(R.string.error_title_required));
			screen.titleInput.requestFocus();
			return;
		}
		screen.saveButton.setEnabled(false);
		executorService.execute(() -> {
			try {
				notesRepository.create(title, body, photoUri == null ? null : photoUri.toString());
				requireActivity().runOnUiThread(() -> {
					if (screen != null) screen.saveButton.setEnabled(true);
					((MainActivity) requireActivity()).showTab(1);
				});
			} catch (Exception e) {
				requireActivity().runOnUiThread(() -> {
					if (screen == null) return;
					screen.saveButton.setEnabled(true);
					if (e instanceof ApiException apiException && apiException.getHttpCode() == 401) {
						((MainActivity) requireActivity()).redirectToLogin();
						return;
					}
					Toast.makeText(requireContext(), getString(R.string.save_failed, e.getMessage()), Toast.LENGTH_LONG).show();
				});
			}
		});
	}

	private void showPhotoChooser() {
		new AlertDialog.Builder(requireContext()).setTitle(R.string.add_photo_title)
				.setItems(new String[]{getString(R.string.action_take_photo), getString(R.string.action_choose_from_gallery)},
						(dialog, which) -> {
							if (which == 0) {
								if (!requireContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
									Toast.makeText(requireContext(), R.string.device_has_no_camera, Toast.LENGTH_SHORT).show();
								} else if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
									launchCamera();
								} else requestPermissionLauncher.launch(Manifest.permission.CAMERA);
							} else pickImageLauncher.launch(new PickVisualMediaRequest.Builder()
									.setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
						}).show();
	}

	private void launchCamera() {
		photoFile = createPhotoFile();
		photoUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", photoFile);
		takePhotoLauncher.launch(photoUri);
	}

	private void retryCameraPermission() {
		requestPermissionLauncher.launch(Manifest.permission.CAMERA);
	}

	private void importPickedImage(Uri sourceUri) {
		File destination = createPhotoFile();
		executorService.execute(() -> {
			try (InputStream input = requireContext().getContentResolver().openInputStream(sourceUri)) {
				if (input == null) throw new IOException("Could not open " + sourceUri);
				Files.copy(input, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
				Uri imported = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", destination);
				photoFile = destination;
				photoUri = imported;
				requireActivity().runOnUiThread(() -> {
					if (screen == null) return;
					screen.photoCard.setVisibility(View.VISIBLE);
					Glide.with(this).load(imported).centerCrop().into(screen.photoPreview);
				});
			} catch (IOException e) {
				Log.e("CreateNoteFragment", "Could not import picked image", e);
				requireActivity().runOnUiThread(() -> {
					photoFile = null;
					photoUri = null;
					if (screen != null) screen.photoCard.setVisibility(View.GONE);
					Toast.makeText(requireContext(), R.string.image_import_failed, Toast.LENGTH_SHORT).show();
				});
			}
		});
	}

	private File createPhotoFile() {
		String name = "todoapp-photo-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".jpg";
		return new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), name);
	}

	private void restorePhoto(String path) {
		if (path == null) return;
		photoFile = new File(path);
		photoUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", photoFile);
		screen.photoCard.setVisibility(View.VISIBLE);
		Glide.with(this).load(photoUri).centerCrop().into(screen.photoPreview);
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		if (photoFile != null) outState.putString(STATE_PHOTO_PATH, photoFile.getAbsolutePath());
		super.onSaveInstanceState(outState);
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
}
