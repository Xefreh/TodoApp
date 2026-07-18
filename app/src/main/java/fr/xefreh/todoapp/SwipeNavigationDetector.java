package fr.xefreh.todoapp;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

/** Detects deliberate horizontal flings without consuming touch events from child views. */
final class SwipeNavigationDetector {
	private static final float HORIZONTAL_DOMINANCE = 1.25f;

	private final GestureDetector gestureDetector;

	SwipeNavigationDetector(Context context, Direction direction, Runnable onSwipe) {
		float density = context.getResources().getDisplayMetrics().density;
		float minimumDistance = 72 * density;
		float minimumVelocity = 450 * density;

		gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDown(@NonNull MotionEvent event) {
				return true;
			}

			@Override
			public boolean onFling(MotionEvent start, @NonNull MotionEvent end, float velocityX, float velocityY) {
				if (start == null) {
					return false;
				}

				float distanceX = end.getX() - start.getX();
				float distanceY = end.getY() - start.getY();
				boolean isHorizontal = Math.abs(distanceX) > Math.abs(distanceY) * HORIZONTAL_DOMINANCE;
				boolean isLongEnough = Math.abs(distanceX) >= minimumDistance;
				boolean isFastEnough = Math.abs(velocityX) >= minimumVelocity;
				boolean isExpectedDirection = direction == Direction.LEFT
						? distanceX < 0 && velocityX < 0
						: distanceX > 0 && velocityX > 0;

				if (isHorizontal && isLongEnough && isFastEnough && isExpectedDirection) {
					onSwipe.run();
					return true;
				}
				return false;
			}
		});
	}

	void onTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);
	}

	enum Direction {
		LEFT,
		RIGHT
	}
}
