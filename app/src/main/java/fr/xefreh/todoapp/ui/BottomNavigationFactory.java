package fr.xefreh.todoapp.ui;

import android.content.Context;
import android.view.Menu;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import fr.xefreh.todoapp.R;

final class BottomNavigationFactory {
	private static final int NAVIGATION_GROUP_ID = Menu.NONE;
	private static final int CREATE_ITEM_ORDER = 0;
	private static final int NOTES_ITEM_ORDER = 1;

	static final int NAV_CREATE = 1;
	static final int NAV_NOTES = 2;

	private BottomNavigationFactory() {
	}

	static BottomNavigationView create(Context context) {
		BottomNavigationView navigation = new BottomNavigationView(context);
		navigation.setId(ViewGroup.generateViewId());

		Menu menu = navigation.getMenu();
		menu.add(NAVIGATION_GROUP_ID, NAV_CREATE, CREATE_ITEM_ORDER, R.string.nav_new_note)
				.setIcon(R.drawable.ic_edit_24);
		menu.add(NAVIGATION_GROUP_ID, NAV_NOTES, NOTES_ITEM_ORDER, R.string.nav_notes)
				.setIcon(R.drawable.ic_list_24);
		return navigation;
	}
}
