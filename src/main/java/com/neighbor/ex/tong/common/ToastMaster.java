package com.neighbor.ex.tong.common;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ToastMaster extends Toast {
	
	private static Toast sToast = null;
	private static final int TOAST_HEIGHT = 120;

	/**
	 * Constructor
	 * 
	 * @param Context context
	 */
	public ToastMaster(Context context) {
		super(context);
	}
	
    public static void cancelToast() {
        if (sToast != null)
            sToast.cancel();
        sToast = null;
    }

	/**
	 * show Toast short
	 * 
	 * @param context Context
	 * @param int resId
	 */
	public static void showShort(Context context, int resId) {
		cancelToast();
		sToast = makeText(context, resId, LENGTH_SHORT);
		sToast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, (int)convertDpToPixel(context, TOAST_HEIGHT));
		sToast.show();
	}

	/**
	 * show Toast short
	 * 
	 * @param context Context
	 * @param CharSequence text
	 */
	public static void showShort(Context context, CharSequence text) {
		cancelToast();
		sToast = makeText(context, text, LENGTH_SHORT);
		sToast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, (int)convertDpToPixel(context, TOAST_HEIGHT));
		sToast.show();
	}
	
	/**
	 * show Toast long
	 * 
	 * @param context Context
	 * @param int resId
	 */
	public static void showLong(Context context, int resId) {
		cancelToast();
		sToast = makeText(context, resId, LENGTH_LONG);
		sToast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, (int)convertDpToPixel(context, TOAST_HEIGHT));
		sToast.show();
		
	}

	/**
	 * show Toast long
	 * 
	 * @param context Context
	 * @param CharSequence text
	 */
	public static void showLong(Context context, CharSequence text) {
		cancelToast();
		sToast = makeText(context, text, LENGTH_LONG);
		sToast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, (int)convertDpToPixel(context, TOAST_HEIGHT));
		LinearLayout toastLayout = (LinearLayout) sToast.getView();
		TextView toastTV = (TextView) toastLayout.getChildAt(0);
		toastTV.setTextSize(30);
		sToast.show();
	}

	
	
	public static float convertDpToPixel(Context context, float dp){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi / 160f);
	    return px;
	}

	public static float convertPixelsToDp(Context context, float px){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float dp = px / (metrics.densityDpi / 160f);
	    return dp;
	}
	
}