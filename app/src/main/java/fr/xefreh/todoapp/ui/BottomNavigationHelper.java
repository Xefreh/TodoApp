package fr.xefreh.todoapp.ui;

import android.content.Context;
import android.view.Menu;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import fr.xefreh.todoapp.R;

public final class BottomNavigationHelper {
	private static final int NAVIGATION_GROUP_ID = Menu.NONE;
	private static final int CREATE_ITEM_ORDER = 0;
	private static final int NOTES_ITEM_ORDER = 1;
	private static final int PROFILE_ITEM_ORDER = 2;

	static final int NAV_CREATE = 1;
	static final int NAV_NOTES = 2;
	static final int NAV_PROFILE = 3;

	private BottomNavigationHelper() {
	}

	public static BottomNavigationView create(Context context) {
		BottomNavigationView navigation = new BottomNavigationView(context);
		navigation.setId(ViewGroup.generateViewId());

		Menu menu = navigation.getMenu();
		menu.add(NAVIGATION_GROUP_ID, NAV_CREATE, CREATE_ITEM_ORDER, R.string.nav_new_note)
				.setIcon(R.drawable.ic_edit_24);
		menu.add(NAVIGATION_GROUP_ID, NAV_NOTES, NOTES_ITEM_ORDER, R.string.nav_notes)
				.setIcon(R.drawable.ic_list_24);
		menu.add(NAVIGATION_GROUP_ID, NAV_PROFILE, PROFILE_ITEM_ORDER, R.string.nav_profile)
				.setIcon(R.drawable.ic_person_24);
		return navigation;
	}

	public static void selectCreate(BottomNavigationView navigation) {
		navigation.setSelectedItemId(NAV_CREATE);
	}

	public static void selectNotes(BottomNavigationView navigation) {
		navigation.setSelectedItemId(NAV_NOTES);
	}

	public static void selectProfile(BottomNavigationView navigation) {
		navigation.setSelectedItemId(NAV_PROFILE);
	}

	public static boolean isCreateItem(int itemId) {
		return itemId == NAV_CREATE;
	}

	public static boolean isNotesItem(int itemId) {
		return itemId == NAV_NOTES;
	}

	public static boolean isProfileItem(int itemId) {
		return itemId == NAV_PROFILE;
	}
}
