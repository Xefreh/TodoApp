package fr.xefreh.todoapp.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatImageView;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AppBottomNavigationView extends LinearLayout {
	public interface OnItemSelectedListener {
		boolean onItemSelected(int itemId);
	}

	private final Map<Integer, NavigationItem> items = new LinkedHashMap<>();
	private final int selectedColor;
	private final int defaultColor;
	private OnItemSelectedListener listener;
	private int selectedItemId;

	public AppBottomNavigationView(Context context) {
		super(context);
		setOrientation(HORIZONTAL);
		setGravity(Gravity.CENTER_VERTICAL);
		setBackgroundColor(resolveColor(context,
				com.google.android.material.R.attr.colorSurface, Color.WHITE));
		setElevation(ViewUtils.dp(context, 8));
		selectedColor = resolveColor(context,
				com.google.android.material.R.attr.colorPrimary, Color.BLACK);
		defaultColor = resolveColor(context,
				com.google.android.material.R.attr.colorOnSurfaceVariant, Color.DKGRAY);
	}

	public void addItem(int itemId, @DrawableRes int iconResource, @StringRes int labelResource) {
		NavigationItem item = new NavigationItem(getContext(), iconResource, labelResource);
		item.root.setOnClickListener(v -> {
			if (listener != null && listener.onItemSelected(itemId)) {
				setSelectedItemId(itemId);
			}
		});
		items.put(itemId, item);
		addView(item.root, new LinearLayout.LayoutParams(
				0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
		refreshSelection();
	}

	public void setOnItemSelectedListener(OnItemSelectedListener listener) {
		this.listener = listener;
	}

	public void setSelectedItemId(int itemId) {
		selectedItemId = itemId;
		refreshSelection();
	}

	private void refreshSelection() {
		for (Map.Entry<Integer, NavigationItem> entry : items.entrySet()) {
			boolean selected = entry.getKey() == selectedItemId;
			NavigationItem item = entry.getValue();
			int color = selected ? selectedColor : defaultColor;
			item.icon.setImageTintList(ColorStateList.valueOf(color));
			item.label.setTextColor(color);
			item.root.setSelected(selected);
		}
	}

	private static int resolveColor(Context context, int attribute, int fallback) {
		TypedValue value = new TypedValue();
		if (!context.getTheme().resolveAttribute(attribute, value, true)) {
			return fallback;
		}
		return value.resourceId != 0
				? context.getColor(value.resourceId)
				: value.data;
	}

	private static final class NavigationItem {
		private final LinearLayout root;
		private final ImageView icon;
		private final TextView label;

		private NavigationItem(Context context, int iconResource, int labelResource) {
			root = new LinearLayout(context);
			root.setOrientation(VERTICAL);
			root.setGravity(Gravity.CENTER);
			root.setClickable(true);
			root.setFocusable(true);
			int horizontalPadding = ViewUtils.dp(context, 8);
			int verticalPadding = ViewUtils.dp(context, 6);
			root.setPadding(horizontalPadding, verticalPadding,
					horizontalPadding, verticalPadding);

			icon = new AppCompatImageView(context);
			icon.setImageResource(iconResource);
			icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			root.addView(icon, new LinearLayout.LayoutParams(
					ViewUtils.dp(context, 24), ViewUtils.dp(context, 24)));

			label = new TextView(context);
			label.setText(labelResource);
			label.setGravity(Gravity.CENTER);
			label.setTextAppearance(ViewUtils.resolveStyle(
					context, com.google.android.material.R.attr.textAppearanceLabelMedium));
			LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			labelParams.topMargin = ViewUtils.dp(context, 2);
			root.addView(label, labelParams);
		}
	}
}
