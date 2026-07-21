package fr.xefreh.todoapp.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import fr.xefreh.todoapp.R;

public final class NoteItemView extends MaterialCardView {
	public final ImageView photoPreview;
	public final TextView titleText;
	public final TextView bodyText;

	public NoteItemView(Context context) {
		super(context);
		setRadius(ViewUtils.dp(context, 12));
		setLayoutParams(createRecyclerLayoutParams(context));

		LinearLayout container = new LinearLayout(context);
		container.setOrientation(LinearLayout.VERTICAL);
		addView(container, new LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));

		photoPreview = new ImageView(context);
		photoPreview.setContentDescription(context.getString(R.string.note_photo_description));
		photoPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
		container.addView(photoPreview, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewUtils.dp(context, 180)));

		LinearLayout textContainer = new LinearLayout(context);
		textContainer.setOrientation(LinearLayout.VERTICAL);
		int padding = ViewUtils.dp(context, 16);
		textContainer.setPadding(padding, padding, padding, padding);
		container.addView(textContainer, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));

		titleText = new TextView(context);
		titleText.setTextAppearance(ViewUtils.resolveStyle(
				context, com.google.android.material.R.attr.textAppearanceTitleMedium));
		textContainer.addView(titleText, ViewUtils.matchWidthWrapHeight());

		bodyText = new TextView(context);
		bodyText.setTextAppearance(ViewUtils.resolveStyle(
				context, com.google.android.material.R.attr.textAppearanceBodyMedium));
		bodyText.setTextColor(resolveSecondaryTextColors(context));
		bodyText.setEllipsize(TextUtils.TruncateAt.END);
		bodyText.setMaxLines(3);
		LinearLayout.LayoutParams bodyParams = ViewUtils.matchWidthWrapHeight();
		bodyParams.topMargin = ViewUtils.dp(context, 4);
		textContainer.addView(bodyText, bodyParams);
	}

	private static RecyclerView.LayoutParams createRecyclerLayoutParams(Context context) {
		RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		params.setMargins(ViewUtils.dp(context, 16), ViewUtils.dp(context, 6),
				ViewUtils.dp(context, 16), ViewUtils.dp(context, 6));
		return params;
	}

	private static ColorStateList resolveSecondaryTextColors(Context context) {
		TypedValue value = new TypedValue();
		context.getTheme().resolveAttribute(android.R.attr.textColorSecondary, value, true);
		return value.resourceId != 0
				? context.getColorStateList(value.resourceId)
				: ColorStateList.valueOf(value.data);
	}

}
