package fr.xefreh.todoapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fr.xefreh.todoapp.data.SessionManager;
import fr.xefreh.todoapp.ui.ProfileScreen;

public class ProfileFragment extends Fragment {
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		SessionManager sessionManager = new SessionManager(requireContext());
		if (!sessionManager.isLoggedIn()) {
			((MainActivity) requireActivity()).redirectToLogin();
			return new View(requireContext());
		}
		ProfileScreen screen = new ProfileScreen(requireContext());
		screen.username.setText(sessionManager.getUsername());
		screen.logoutButton.setOnClickListener(v -> {
			sessionManager.clear();
			((MainActivity) requireActivity()).redirectToLogin();
		});
		return screen.root;
	}
}
