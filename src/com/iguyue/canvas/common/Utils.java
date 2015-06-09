package com.iguyue.canvas.common;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

public class Utils {
	/**
	 * dp转px
	 * 
	 * @param context
	 * @param dp
	 * @return
	 */
	public static float dp2px(Context context, float dp) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				metrics);
	}

	/**
	 * sp转px
	 * 
	 * @param context
	 * @param dp
	 * @return
	 */
	public static float sp2px(Context context, float sp) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
				metrics);
	}
}
