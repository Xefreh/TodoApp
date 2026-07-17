package fr.xefreh.todoapp.ui;

import android.content.Context;
import android.util.TypedValue;

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
}
