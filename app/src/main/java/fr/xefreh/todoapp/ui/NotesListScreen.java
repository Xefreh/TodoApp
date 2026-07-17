package fr.xefreh.todoapp.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import fr.xefreh.todoapp.R;

public final class NotesListScreen {
	public final ConstraintLayout root;
	public final RecyclerView notesList;
	public final MaterialButton syncButton;
	public final BottomNavigationView bottomNavigation;

	public NotesListScreen(Context context) {
		root = new ConstraintLayout(context);
		root.setId(View.generateViewId());
		root.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		TextView heading = new TextView(context);
		heading.setId(View.generateViewId());
		heading.setText(R.string.title_notes);
		heading.setTextAppearance(ViewUtils.resolveStyle(
				context, com.google.android.material.R.attr.textAppearanceHeadlineSmall));
		root.addView(heading, new ConstraintLayout.LayoutParams(
				0, ViewGroup.LayoutParams.WRAP_CONTENT));

		syncButton = new MaterialButton(context);
		syncButton.setId(View.generateViewId());
		syncButton.setText(R.string.action_sync);
		syncButton.setIconResource(R.drawable.ic_sync_24);
		root.addView(syncButton, new ConstraintLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));

		bottomNavigation = BottomNavigationFactory.create(context);
		root.addView(bottomNavigation, new ConstraintLayout.LayoutParams(
				0, ViewGroup.LayoutParams.WRAP_CONTENT));

		notesList = new RecyclerView(context);
		notesList.setId(View.generateViewId());
		notesList.setClipToPadding(false);
		int verticalPadding = ViewUtils.dp(context, 8);
		notesList.setPadding(0, verticalPadding, 0, verticalPadding);
		root.addView(notesList, new ConstraintLayout.LayoutParams(0, 0));

		int margin16 = ViewUtils.dp(context, 16);
		ConstraintSet constraints = new ConstraintSet();
		constraints.clone(root);
		constraints.connect(heading.getId(), ConstraintSet.START,
				ConstraintSet.PARENT_ID, ConstraintSet.START, margin16);
		constraints.connect(heading.getId(), ConstraintSet.TOP,
				ConstraintSet.PARENT_ID, ConstraintSet.TOP, margin16);
		constraints.connect(heading.getId(), ConstraintSet.END,
				syncButton.getId(), ConstraintSet.START);
		constraints.connect(syncButton.getId(), ConstraintSet.END,
				ConstraintSet.PARENT_ID, ConstraintSet.END, ViewUtils.dp(context, 8));
		constraints.connect(syncButton.getId(), ConstraintSet.TOP,
				heading.getId(), ConstraintSet.TOP);
		constraints.connect(syncButton.getId(), ConstraintSet.BOTTOM,
				heading.getId(), ConstraintSet.BOTTOM);
		constraints.connect(bottomNavigation.getId(), ConstraintSet.START,
				ConstraintSet.PARENT_ID, ConstraintSet.START);
		constraints.connect(bottomNavigation.getId(), ConstraintSet.END,
				ConstraintSet.PARENT_ID, ConstraintSet.END);
		constraints.connect(bottomNavigation.getId(), ConstraintSet.BOTTOM,
				ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
		constraints.connect(notesList.getId(), ConstraintSet.START,
				ConstraintSet.PARENT_ID, ConstraintSet.START);
		constraints.connect(notesList.getId(), ConstraintSet.END,
				ConstraintSet.PARENT_ID, ConstraintSet.END);
		constraints.connect(notesList.getId(), ConstraintSet.TOP,
				heading.getId(), ConstraintSet.BOTTOM);
		constraints.connect(notesList.getId(), ConstraintSet.BOTTOM,
				bottomNavigation.getId(), ConstraintSet.TOP);
		constraints.applyTo(root);
	}

	public void selectNotesNavigationItem() {
		bottomNavigation.setSelectedItemId(BottomNavigationFactory.NAV_NOTES);
	}

	public boolean isCreateNavigationItem(int itemId) {
		return itemId == BottomNavigationFactory.NAV_CREATE;
	}
}
