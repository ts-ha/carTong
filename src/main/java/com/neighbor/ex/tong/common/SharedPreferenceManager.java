package com.neighbor.ex.tong.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferenceManager {
    public static String positionAgree = "IS_AGREE";

    private static SharedPreferences getSharedPreferences(Context context) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
        return preference;
    }

    public static String getValue(Context context, String key) {
        return getSharedPreferences(context).getString(key, "");
    }

    public static boolean setValue(Context context, String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(key, value);
        return editor.commit();
    }

}
