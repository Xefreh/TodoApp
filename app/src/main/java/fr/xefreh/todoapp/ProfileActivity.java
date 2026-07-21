package fr.xefreh.todoapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import fr.xefreh.todoapp.data.SessionManager;
import fr.xefreh.todoapp.ui.BottomNavigationHelper;
import fr.xefreh.todoapp.ui.ProfileScreen;

public class ProfileActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);

		SessionManager sessionManager = new SessionManager(this);
		if (!sessionManager.isLoggedIn()) {
			openLogin();
			return;
		}

		ProfileScreen screen = new ProfileScreen(this);
		setContentView(screen.root);
		ViewCompat.setOnApplyWindowInsetsListener(screen.root, (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});

		screen.username.setText(sessionManager.getUsername());
		BottomNavigationHelper.selectProfile(screen.bottomNavigation);
		screen.bottomNavigation.setOnItemSelectedListener(item -> {
			if (BottomNavigationHelper.isCreateItem(item.getItemId())) {
				openCreateNote();
			} else if (BottomNavigationHelper.isNotesItem(item.getItemId())) {
				startActivity(new Intent(this, NotesListActivity.class));
				finish();
			}
			return false;
		});

		screen.logoutButton.setOnClickListener(v -> {
			sessionManager.clear();
			openLogin();
		});
	}

	private void openCreateNote() {
		Intent intent = new Intent(this, CreateNoteActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
		finish();
	}

	private void openLogin() {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
		finish();
	}
}
