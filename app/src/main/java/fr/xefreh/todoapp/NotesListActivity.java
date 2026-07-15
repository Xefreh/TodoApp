package fr.xefreh.todoapp;

import android.os.Bundle;
import android.util.Log;
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

import fr.xefreh.todoapp.databinding.ActivityNotesListBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotesListActivity extends AppCompatActivity {

    private final AppDatabase appDatabase = DatabaseProvider.getDatabase(this);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ActivityNotesListBinding binding = ActivityNotesListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding.notesList.setLayoutManager(new LinearLayoutManager(this));


        NotesViewModel viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new NotesViewModel(appDatabase);
            }
        }).get(NotesViewModel.class);

        viewModel.getNotes().observe(this, notes -> setNotesAdapter(notes, binding));

        binding.syncBtn.setOnClickListener((v -> {
            JsonPlaceholderApi retrofitApi = RetrofitProvider.getApi();
            retrofitApi.getPosts().enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<List<PostResponse>> call, @NonNull Response<List<PostResponse>> response) {
                    List<PostResponse> posts = response.body();

                    if (posts == null || posts.isEmpty()) {
                        Toast.makeText(NotesListActivity.this, "No posts to sync", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    executorService.execute(() -> posts.forEach((p) -> appDatabase.noteDao().insert(new Note(p.getId(), p.getTitle(), p.getBody()))));
                }

                @Override
                public void onFailure(@NonNull Call<List<PostResponse>> call, @NonNull Throwable t) {
                    Log.e("NotesListActivity", "Fetch failed", t);
                }
            });
        }));
    }

    private static void setNotesAdapter(List<Note> notes, ActivityNotesListBinding binding) {
        NotesAdapter notesAdapter = new NotesAdapter(notes);
        binding.notesList.setAdapter(notesAdapter);
    }
}