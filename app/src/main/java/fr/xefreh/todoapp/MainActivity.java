package fr.xefreh.todoapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

public class MainActivity extends AppCompatActivity {

	private MainScreen screen;
	private File photoFile;
	private Uri photoUri;
	private SwipeNavigationDetector swipeNavigationDetector;
	private boolean isOpeningNotes;
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private NotesRepository notesRepository;

	private final ActivityResultLauncher<Uri> takePhotoLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), wasSaved -> {
		if (wasSaved) {
			screen.photoCard.setVisibility(View.VISIBLE);
			Glide.with(this).load(photoUri).centerCrop().into(screen.photoPreview);
			Toast.makeText(MainActivity.this, "Photo saved to " + photoFile.getPath(), Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(MainActivity.this, "Could not save photo", Toast.LENGTH_SHORT).show();
		}
	});

	private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
		if (isGranted) {
			launchCamera();
		} else {
			boolean shouldShow = shouldShowRequestPermissionRationale(Manifest.permission.CAMERA);
			if (shouldShow) {
				new AlertDialog.Builder(this).setTitle("Camera access needed").setMessage("This lets you attach a photo directly to your notes. Without it, you can still add photos from your gallery.").setPositiveButton("Try Again", (dialog, which) -> MainActivity.this.requestPermissionLauncher.launch(Manifest.permission.CAMERA)).setNegativeButton("Cancel", null).show();
			} else {
				Toast.makeText(MainActivity.this, "Camera permission is disabled. Enable it in Settings", Toast.LENGTH_SHORT).show();
			}
		}
	});

	private final ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
		if (uri != null) {
			screen.photoCard.setVisibility(View.VISIBLE);
			Glide.with(this).load(uri).centerCrop().into(screen.photoPreview);
			importPickedImage(uri);
		}
	});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);

		notesRepository = new NotesRepositoryImpl(
				RetrofitProvider.getApi(),
				new SessionManager(this));

		screen = new MainScreen(this);
		setContentView(screen.root);
		swipeNavigationDetector = new SwipeNavigationDetector(
				this,
				SwipeNavigationDetector.Direction.LEFT,
				this::openNotes);

		ViewCompat.setOnApplyWindowInsetsListener(screen.root, (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});

		screen.selectCreateNavigationItem();
		screen.bottomNavigation.setOnItemSelectedListener(item -> {
			if (screen.isNotesNavigationItem(item.getItemId())) {
				openNotes();
			}
			// Keep "New note" highlighted: this activity stays below the notes screen.
			return false;
		});

		screen.titleInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (!s.toString().trim().isEmpty()) {
					screen.titleInputLayout.setError(null);
				}
			}
		});

		screen.saveButton.setOnClickListener((v) -> {
			String title = screen.titleInput.getText().toString().trim();
			String body = screen.bodyInput.getText().toString();

			if (title.isEmpty()) {
				screen.titleInputLayout.setError(getString(R.string.error_title_required));
				screen.titleInput.requestFocus();
				return;
			}

			screen.saveButton.setEnabled(false);
			// Read photoUri inside the task: the single-threaded executor guarantees a
			// pending gallery-image copy submitted earlier has finished by then.
			executorService.execute(() -> {
				String imageUri = photoUri != null ? photoUri.toString() : null;
				try {
					notesRepository.create(title, body, imageUri);
					runOnUiThread(() -> {
						screen.saveButton.setEnabled(true);
						startActivity(new Intent(MainActivity.this, NotesListActivity.class));
					});
				} catch (Exception e) {
					// Catch-all: the save button must always be re-enabled, whatever the
					// failure (ApiException, but also any unexpected RuntimeException).
					runOnUiThread(() -> {
						screen.saveButton.setEnabled(true);
						if (e instanceof ApiException apiException
								&& apiException.getHttpCode() == 401) {
							redirectToLogin();
							return;
						}
						Toast.makeText(MainActivity.this,
								"Save failed: " + e.getMessage(),
								Toast.LENGTH_LONG).show();
					});
				}
			});
		});

		screen.attachPhotoButton.setOnClickListener((v -> {
			new AlertDialog.Builder(this).setTitle("Add Photo").setItems(new String[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
				if (which == 0) {
					boolean hasCamera = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);

					if (!hasCamera) {
						Toast.makeText(MainActivity.this, "Device has no camera", Toast.LENGTH_SHORT).show();
						return;
					}

					if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
						launchCamera();
					} else {
						requestPermissionLauncher.launch(Manifest.permission.CAMERA);
					}
				} else {
					pickImageLauncher.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
				}
			}).show();
		}));
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (swipeNavigationDetector != null) {
			swipeNavigationDetector.onTouchEvent(event);
		}
		return super.dispatchTouchEvent(event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		isOpeningNotes = false;
	}

	private void openNotes() {
		if (isOpeningNotes) {
			return;
		}
		isOpeningNotes = true;
		startActivity(new Intent(this, NotesListActivity.class));
	}

	/** The session expired or was revoked: back to the login screen, clearing the task. */
	private void redirectToLogin() {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
		finish();
	}

	private void launchCamera() {
		photoFile = createPhotoFile();
		photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
		takePhotoLauncher.launch(photoUri);
	}

	private void importPickedImage(Uri sourceUri) {
		File destFile = createPhotoFile();
		executorService.execute(() -> {
			try (InputStream in = getContentResolver().openInputStream(sourceUri)) {
				if (in == null) {
					throw new IOException("Could not open " + sourceUri);
				}
				Files.copy(in, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				photoFile = destFile;
				photoUri = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".fileprovider", destFile);
			} catch (IOException e) {
				Log.e("MainActivity", "Could not import picked image", e);
				runOnUiThread(() -> Toast.makeText(MainActivity.this, "Could not import image", Toast.LENGTH_SHORT).show());
			}
		});
	}

	private File createPhotoFile() {
		String photoName = "todoapp-photo-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".jpg";
		return new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), photoName);
	}
}
