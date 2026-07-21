package fr.xefreh.todoapp.ui;

import android.content.Context;
import android.view.ViewGroup;

import fr.xefreh.todoapp.R;

public final class BottomNavigationHelper {
	static final int NAV_CREATE = 1;
	static final int NAV_NOTES = 2;
	static final int NAV_PROFILE = 3;

	private BottomNavigationHelper() {
	}

	public static AppBottomNavigationView create(Context context) {
		AppBottomNavigationView navigation = new AppBottomNavigationView(context);
		navigation.setId(ViewGroup.generateViewId());
		navigation.addItem(NAV_CREATE, R.drawable.ic_edit_24, R.string.nav_new_note);
		navigation.addItem(NAV_NOTES, R.drawable.ic_list_24, R.string.nav_notes);
		navigation.addItem(NAV_PROFILE, R.drawable.ic_person_24, R.string.nav_profile);
		return navigation;
	}

	public static void selectCreate(AppBottomNavigationView navigation) {
		navigation.setSelectedItemId(NAV_CREATE);
	}

	public static void selectNotes(AppBottomNavigationView navigation) {
		navigation.setSelectedItemId(NAV_NOTES);
	}

	public static void selectProfile(AppBottomNavigationView navigation) {
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

	public static int tabForItem(int itemId) {
		if (isCreateItem(itemId)) {
			return 0;
		}
		if (isNotesItem(itemId)) {
			return 1;
		}
		return 2;
	}
}
