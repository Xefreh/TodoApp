package fr.xefreh.todoapp.ui;

import android.content.Context;
import android.view.Menu;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import fr.xefreh.todoapp.R;

final class BottomNavigationFactory {
	static final int NAV_CREATE = 1;
	static final int NAV_NOTES = 2;

	private BottomNavigationFactory() {
	}

	static BottomNavigationView create(Context context) {
		BottomNavigationView navigation = new BottomNavigationView(context);
		navigation.setId(ViewGroup.generateViewId());

		Menu menu = navigation.getMenu();
		menu.add(Menu.NONE, NAV_CREATE, 0, R.string.nav_new_note)
				.setIcon(R.drawable.ic_edit_24);
		menu.add(Menu.NONE, NAV_NOTES, 1, R.string.nav_notes)
				.setIcon(R.drawable.ic_list_24);
		return navigation;
	}
}
