package com.onedialogproject.galapagosmapho;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private static final String PREFERENCE_FILENAME = "Galapagosmapho";
    private static final String MAIN_SETTING = "main_setting";
    private static final String WIFI_SETTING = "wifi_setting";
    private static final String RECONNECT_DURATION = "reconnect_duration";
    private static final String DEBUG_MODE = "debug_mode";

    public static void setDebugMode(Context context, boolean enabled) {
        SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(DEBUG_MODE, enabled);
        editor.commit();
    }

    public static boolean getDebugMode(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        return sp.getBoolean(DEBUG_MODE, false);
    }

    public static void setMainSetting(Context context, boolean setting) {
        SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(MAIN_SETTING, setting);
        editor.commit();
    }

    public static boolean getMainSetting(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        return sp.getBoolean(MAIN_SETTING, false);
    }

    public static void setWifiSetting(Context context, boolean setting) {
        SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(WIFI_SETTING, setting);
        editor.commit();
    }

    public static boolean getWifiSetting(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        return sp.getBoolean(WIFI_SETTING, false);
    }

    public static void setReconnectDuration(Context context, int duration) {
        SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(RECONNECT_DURATION, duration);
        editor.commit();
    }

    public static int getReconnectDuration(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        return sp.getInt(RECONNECT_DURATION, 0);
    }
}
