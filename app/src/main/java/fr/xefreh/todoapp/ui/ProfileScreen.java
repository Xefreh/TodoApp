package fr.xefreh.todoapp.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.button.MaterialButton;

import fr.xefreh.todoapp.R;

public final class ProfileScreen {
	public final ConstraintLayout root;
	public final TextView username;
	public final MaterialButton logoutButton;

	public ProfileScreen(Context context) {
		root = new ConstraintLayout(context);
		root.setId(View.generateViewId());
		root.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		NestedScrollView scroll = new NestedScrollView(context);
		scroll.setId(View.generateViewId());
		scroll.setFillViewport(true);
		root.addView(scroll, new ConstraintLayout.LayoutParams(0, 0));

		LinearLayout content = new LinearLayout(context);
		content.setOrientation(LinearLayout.VERTICAL);
		int padding = ViewUtils.dp(context, 16);
		content.setPadding(padding, padding, padding, padding);
		scroll.addView(content, new NestedScrollView.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));

		TextView heading = new TextView(context);
		heading.setText(R.string.title_profile);
		heading.setTextAppearance(ViewUtils.resolveStyle(
				context, com.google.android.material.R.attr.textAppearanceHeadlineSmall));
		LinearLayout.LayoutParams headingParams = ViewUtils.matchWidthWrapHeight();
		headingParams.bottomMargin = padding;
		content.addView(heading, headingParams);

		TextView usernameLabel = new TextView(context);
		usernameLabel.setText(R.string.label_username);
		usernameLabel.setTextAppearance(ViewUtils.resolveStyle(
				context, com.google.android.material.R.attr.textAppearanceLabelLarge));
		content.addView(usernameLabel, ViewUtils.matchWidthWrapHeight());

		username = new TextView(context);
		username.setTextAppearance(ViewUtils.resolveStyle(
				context, com.google.android.material.R.attr.textAppearanceBodyLarge));
		LinearLayout.LayoutParams usernameParams = ViewUtils.matchWidthWrapHeight();
		usernameParams.topMargin = ViewUtils.dp(context, 4);
		content.addView(username, usernameParams);

		logoutButton = new MaterialButton(
				context, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
		logoutButton.setText(R.string.action_logout);
		logoutButton.setIconResource(R.drawable.ic_logout_24);
		LinearLayout.LayoutParams logoutParams = ViewUtils.matchWidthWrapHeight();
		logoutParams.topMargin = padding;
		content.addView(logoutButton, logoutParams);

		ConstraintSet constraints = new ConstraintSet();
		constraints.clone(root);
		constraints.connect(scroll.getId(), ConstraintSet.START,
				ConstraintSet.PARENT_ID, ConstraintSet.START);
		constraints.connect(scroll.getId(), ConstraintSet.END,
				ConstraintSet.PARENT_ID, ConstraintSet.END);
		constraints.connect(scroll.getId(), ConstraintSet.TOP,
				ConstraintSet.PARENT_ID, ConstraintSet.TOP);
		constraints.connect(scroll.getId(), ConstraintSet.BOTTOM,
				ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
		constraints.applyTo(root);
	}

}
