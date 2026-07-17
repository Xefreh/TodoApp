package fr.xefreh.todoapp.ui;

import android.content.Context;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import fr.xefreh.todoapp.R;

public final class MainScreen {
	public final ConstraintLayout root;
	public final TextInputLayout titleInputLayout;
	public final TextInputEditText titleInput;
	public final TextInputEditText bodyInput;
	public final MaterialButton attachPhotoButton;
	public final MaterialCardView photoCard;
	public final ImageView photoPreview;
	public final MaterialButton saveButton;
	public final BottomNavigationView bottomNavigation;

	public MainScreen(Context context) {
		root = new ConstraintLayout(context);
		root.setId(View.generateViewId());
		root.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		bottomNavigation = BottomNavigationFactory.create(context);
		root.addView(bottomNavigation, new ConstraintLayout.LayoutParams(
				0, ViewGroup.LayoutParams.WRAP_CONTENT));

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
		heading.setText(R.string.title_new_note);
		heading.setTextAppearance(ViewUtils.resolveStyle(
				context, com.google.android.material.R.attr.textAppearanceHeadlineSmall));
		LinearLayout.LayoutParams headingParams = matchWrap();
		headingParams.bottomMargin = padding;
		content.addView(heading, headingParams);

		titleInputLayout = new TextInputLayout(
				context, null, com.google.android.material.R.attr.textInputOutlinedStyle);
		titleInputLayout.setHint(R.string.hint_title);
		titleInput = new TextInputEditText(titleInputLayout.getContext());
		titleInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		titleInputLayout.addView(titleInput, matchWrap());
		content.addView(titleInputLayout, matchWrap());

		TextInputLayout bodyInputLayout = new TextInputLayout(
				context, null, com.google.android.material.R.attr.textInputOutlinedStyle);
		bodyInputLayout.setHint(R.string.hint_body);
		bodyInput = new TextInputEditText(bodyInputLayout.getContext());
		bodyInput.setGravity(Gravity.TOP);
		bodyInput.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_MULTI_LINE
				| InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		bodyInput.setMinLines(6);
		bodyInputLayout.addView(bodyInput, matchWrap());
		LinearLayout.LayoutParams bodyParams = matchWrap();
		bodyParams.topMargin = ViewUtils.dp(context, 12);
		content.addView(bodyInputLayout, bodyParams);

		attachPhotoButton = new MaterialButton(
				context, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
		attachPhotoButton.setText(R.string.action_add_photo);
		attachPhotoButton.setIconResource(R.drawable.baseline_add_a_photo_24);
		LinearLayout.LayoutParams attachParams = matchWrap();
		attachParams.topMargin = ViewUtils.dp(context, 12);
		content.addView(attachPhotoButton, attachParams);

		photoCard = new MaterialCardView(context);
		photoCard.setRadius(ViewUtils.dp(context, 12));
		photoCard.setVisibility(View.GONE);
		photoPreview = new ImageView(context);
		photoPreview.setContentDescription(context.getString(R.string.photo_preview_description));
		photoPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
		photoCard.addView(photoPreview, new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewUtils.dp(context, 200)));
		LinearLayout.LayoutParams photoParams = matchWrap();
		photoParams.topMargin = ViewUtils.dp(context, 12);
		content.addView(photoCard, photoParams);

		saveButton = new MaterialButton(context);
		saveButton.setText(R.string.action_save);
		LinearLayout.LayoutParams saveParams = matchWrap();
		saveParams.topMargin = padding;
		content.addView(saveButton, saveParams);

		ConstraintSet constraints = new ConstraintSet();
		constraints.clone(root);
		constraints.connect(bottomNavigation.getId(), ConstraintSet.START,
				ConstraintSet.PARENT_ID, ConstraintSet.START);
		constraints.connect(bottomNavigation.getId(), ConstraintSet.END,
				ConstraintSet.PARENT_ID, ConstraintSet.END);
		constraints.connect(bottomNavigation.getId(), ConstraintSet.BOTTOM,
				ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
		constraints.connect(scroll.getId(), ConstraintSet.START,
				ConstraintSet.PARENT_ID, ConstraintSet.START);
		constraints.connect(scroll.getId(), ConstraintSet.END,
				ConstraintSet.PARENT_ID, ConstraintSet.END);
		constraints.connect(scroll.getId(), ConstraintSet.TOP,
				ConstraintSet.PARENT_ID, ConstraintSet.TOP);
		constraints.connect(scroll.getId(), ConstraintSet.BOTTOM,
				bottomNavigation.getId(), ConstraintSet.TOP);
		constraints.applyTo(root);
	}

	public void selectCreateNavigationItem() {
		bottomNavigation.setSelectedItemId(BottomNavigationFactory.NAV_CREATE);
	}

	public boolean isNotesNavigationItem(int itemId) {
		return itemId == BottomNavigationFactory.NAV_NOTES;
	}

	private static LinearLayout.LayoutParams matchWrap() {
		return new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
	}
}
