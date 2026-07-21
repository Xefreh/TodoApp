package fr.xefreh.todoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;

import fr.xefreh.todoapp.data.SessionManager;
import fr.xefreh.todoapp.ui.AppBottomNavigationView;
import fr.xefreh.todoapp.ui.BottomNavigationHelper;

public class MainActivity extends AppCompatActivity {
	private static final String STATE_TAB = "state_tab";
	private static final String[] TAGS = {"create", "notes", "profile"};

	private final Fragment[] fragments = new Fragment[3];
	private int currentTab = 1;
	private AppBottomNavigationView navigation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		if (!new SessionManager(this).isLoggedIn()) {
			redirectToLogin();
			return;
		}

		FrameLayout root = new FrameLayout(this);
		root.setId(View.generateViewId());
		FrameLayout content = new FrameLayout(this);
		content.setId(R.id.authenticated_content);
		navigation = BottomNavigationHelper.create(this);
		root.addView(content, new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		FrameLayout.LayoutParams navigationParams = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.Gravity.BOTTOM);
		root.addView(navigation, navigationParams);
		setContentView(root);

		ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
			Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			content.setPadding(bars.left, bars.top, bars.right, navigation.getMeasuredHeight());
			navigation.setPadding(bars.left, 0, bars.right, bars.bottom);
			return insets;
		});
		navigation.addOnLayoutChangeListener((v, l, t, r, b, oldL, oldT, oldR, oldB) ->
				ViewCompat.requestApplyInsets(root));

		fragments[0] = getSupportFragmentManager().findFragmentByTag(TAGS[0]);
		fragments[1] = getSupportFragmentManager().findFragmentByTag(TAGS[1]);
		fragments[2] = getSupportFragmentManager().findFragmentByTag(TAGS[2]);
		if (fragments[0] == null) fragments[0] = new CreateNoteFragment();
		if (fragments[1] == null) fragments[1] = new NotesListFragment();
		if (fragments[2] == null) fragments[2] = new ProfileFragment();

		currentTab = savedInstanceState == null ? 1 : savedInstanceState.getInt(STATE_TAB, 1);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		for (int i = 0; i < fragments.length; i++) {
			Fragment fragment = fragments[i];
			if (!fragment.isAdded()) transaction.add(content.getId(), fragment, TAGS[i]);
			if (i == currentTab) {
				transaction.show(fragment).setMaxLifecycle(fragment, Lifecycle.State.RESUMED);
			} else {
				transaction.hide(fragment).setMaxLifecycle(fragment, Lifecycle.State.STARTED);
			}
		}
		transaction.commit();
		selectNavigation(navigation, currentTab);
		navigation.setOnItemSelectedListener(itemId -> {
			showTab(BottomNavigationHelper.tabForItem(itemId));
			return true;
		});
	}

	public void showTab(int tab) {
		if (tab == currentTab) return;
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		if (tab > currentTab) {
			transaction.setCustomAnimations(
					R.anim.slide_in_from_right, R.anim.slide_out_to_left);
		} else {
			transaction.setCustomAnimations(
					R.anim.slide_in_from_left, R.anim.slide_out_to_right);
		}
		transaction
				.hide(fragments[currentTab]).setMaxLifecycle(fragments[currentTab], Lifecycle.State.STARTED)
				.show(fragments[tab]).setMaxLifecycle(fragments[tab], Lifecycle.State.RESUMED)
				.commit();
		currentTab = tab;
		selectNavigation(navigation, tab);
	}

	public void redirectToLogin() {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
		finish();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		Fragment fragment = fragments[currentTab];
		if (fragment instanceof SwipeTarget target) target.onTouchEvent(event);
		return super.dispatchTouchEvent(event);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_TAB, currentTab);
		super.onSaveInstanceState(outState);
	}

	private static void selectNavigation(AppBottomNavigationView navigation, int tab) {
		if (tab == 0) BottomNavigationHelper.selectCreate(navigation);
		else if (tab == 1) BottomNavigationHelper.selectNotes(navigation);
		else BottomNavigationHelper.selectProfile(navigation);
	}

	interface SwipeTarget {
		void onTouchEvent(MotionEvent event);
	}
}
