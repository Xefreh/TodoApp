package fr.xefreh.todoapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.xefreh.todoapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // permission granted — proceed
                } else {
                    // permission denied — handle gracefully
                }
            });

    private final ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    // uri is the selected photo's location — we'll use this to display/save it
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.saveBtn.setOnClickListener((v) -> {
            String title = binding.titleInput.getText().toString();
            String body = binding.bodyInput.getText().toString();

            AppDatabase appDatabase = DatabaseProvider.getDatabase(this);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                appDatabase.noteDao().insert(new Note(title, body));

                Intent intent = new Intent(MainActivity.this, NotesListActivity.class);
                startActivity(intent);
            });
        });

        binding.attachPhotoBtn.setOnClickListener((v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Add Photo")
                    .setItems(new String[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
                        if (which == 0) {
                            boolean hasCamera = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);

                            if (!hasCamera) {
                                Toast.makeText(MainActivity.this, "Device has no camera", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                    == PackageManager.PERMISSION_GRANTED) {
                                // already granted — go ahead and open the camera
                            } else {
                                // not granted — request it
                                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                            }
                        } else {
                            pickImageLauncher.launch(new PickVisualMediaRequest.Builder()
                                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                    .build());
                        }
                    })
                    .show();
        }));
    }
}