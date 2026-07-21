package fr.xefreh.todoapp.ui;

import android.content.Context;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;

final class ViewUtils {
	private ViewUtils() {
	}

	static int dp(Context context, int value) {
		return Math.round(TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				value,
				context.getResources().getDisplayMetrics()));
	}

	static int resolveStyle(Context context, int attribute) {
		TypedValue value = new TypedValue();
		return context.getTheme().resolveAttribute(attribute, value, true)
				? value.resourceId
				: 0;
	}

	static LinearLayout.LayoutParams matchWidthWrapHeight() {
		return new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
	}
}
