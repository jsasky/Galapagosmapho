package com.onedialogproject.galapagosmapho;

import com.onedialogproject.galapagosmapho.ResidentService.ServiceState;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private static final String PREFERENCE_FILENAME = "Galapagosmapho";
    private static final String SERVICE_STATE = "service_state";
    private static final String ACTIVE_FLAG = "active_flag";
    private static final String LTE_3G_SETTING = "main_setting";
    private static final String WIFI_SETTING = "wifi_setting";
    private static final String BLUETOOTH_SETTING = "bluetooth_setting";
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

    public static void setServiceState(Context context,
            ServiceState serviceState) {
        SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(SERVICE_STATE, serviceState.getId());
        editor.commit();
    }

    public static ServiceState getServiceState(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        return ServiceState.getState(sp.getInt(SERVICE_STATE, 0));
    }

    public static void setActiveFlag(Context context, boolean enabled) {
        SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(ACTIVE_FLAG, enabled);
        editor.commit();
    }

    public static boolean getActiveFlag(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        return sp.getBoolean(ACTIVE_FLAG, false);
    }

    public static void setLte3gSetting(Context context, boolean setting) {
        SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(LTE_3G_SETTING, setting);
        editor.commit();
    }

    public static boolean getLte3gSetting(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        return sp.getBoolean(LTE_3G_SETTING, false);
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

    public static void setBluetoothSetting(Context context, boolean setting) {
        SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(BLUETOOTH_SETTING, setting);
        editor.commit();
    }

    public static boolean getBluetoothSetting(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        return sp.getBoolean(BLUETOOTH_SETTING, false);
    }

    public static boolean isActivated(Context context) {
        return (getLte3gSetting(context) || getWifiSetting(context) || getBluetoothSetting(context));
    }
}
