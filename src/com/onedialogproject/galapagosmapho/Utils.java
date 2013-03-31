package com.onedialogproject.galapagosmapho;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;

public class Utils {

    public static enum Device {
        WIFI("wifi", "WiFi"), DATA_3G_LTE("roaming", "3G");

        final String suffix;
        final String log;

        Device(String suffix, String log) {
            this.suffix = suffix;
            this.log = log;
        }
    }

    public static void setDeviceState(Context context, Device device,
            boolean enabled) {
        switch (device) {
        case DATA_3G_LTE: {
            final ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            try {
                Method setMethod = manager.getClass().getMethod(
                        "setMobileDataEnabled", boolean.class);
                setMethod.invoke(manager, enabled);

            } catch (NoSuchMethodException e) {
                DebugTools.printStackTrace(e);
            } catch (IllegalArgumentException e) {
                DebugTools.printStackTrace(e);
            } catch (IllegalAccessException e) {
                DebugTools.printStackTrace(e);
            } catch (InvocationTargetException e) {
                DebugTools.printStackTrace(e);
            }
            break;
        }
        case WIFI: {
            WifiManager wifiManager = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(enabled);
            break;
        }
        default:
            Assert.assertTrue(false);
            break;
        }
    }

    public static boolean getDeviceState(Context context, Device device) {

        switch (device) {
        case DATA_3G_LTE: {
            final ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            boolean ret = true;
            try {
                Method getMethod = manager.getClass().getMethod(
                        "getMobileDataEnabled");
                ret = ((Boolean) getMethod.invoke(manager)).booleanValue();
            } catch (NoSuchMethodException e) {
                DebugTools.printStackTrace(e);
            } catch (IllegalArgumentException e) {
                DebugTools.printStackTrace(e);
            } catch (IllegalAccessException e) {
                DebugTools.printStackTrace(e);
            } catch (InvocationTargetException e) {
                DebugTools.printStackTrace(e);
            }
            return ret;
        }
        case WIFI: {
            WifiManager wifiManager = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);

            return wifiManager.isWifiEnabled();
        }
        default:
            Assert.assertTrue(false);
            break;
        }
        return false;
    }

    private static int MAX_LOG_LENGTH = 10000;

    public static void clearLog(Context context) {
        Prefs.setLog(context, "");
    }

    public static void addLog(Context context, String line) {
        StringBuffer log = new StringBuffer(MAX_LOG_LENGTH);
        log.append(getDateTimeString(context) + " " + line);
        log.append('\n');
        log.append(Prefs.getLog(context));
        if (log.length() > MAX_LOG_LENGTH) {
            Prefs.setLog(context, log.toString().substring(0, MAX_LOG_LENGTH));
        } else {
            Prefs.setLog(context, log.toString());
        }
        DebugTools.routeCheck(line, true);
    }

    private static String getDateTimeString(Context context) {
        final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat(
                "MM/dd kk:mm:ss");
        Date date = new Date();
        return DATETIME_FORMAT.format(date);
    }

    public static boolean isNotifyAreaControllerAvailable(Context context) {
        int iAccessibilityEnabled = 0;
        try {
            iAccessibilityEnabled = Settings.Secure.getInt(
                    context.getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (SettingNotFoundException e) {
        }

        if (iAccessibilityEnabled != 1) {
            return false;
        }

        String settingValue = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

        if (settingValue == null) {
            return false;
        }

        TextUtils.SimpleStringSplitter stringColonSplitter = new TextUtils.SimpleStringSplitter(
                ':');
        stringColonSplitter.setString(settingValue);

        while (stringColonSplitter.hasNext()) {
            String service = stringColonSplitter.next();
            String clazz = service.substring(service.indexOf("/") + 1);
            if (clazz.equals(NotifyAreaController.class.getName())) {
                return true;
            }
        }

        return false;
    }

    public static void notify(Context context) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        final Notification notification = new Notification();
        notification.defaults = Notification.DEFAULT_SOUND
                | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;

        notificationManager.notify(R.string.app_name, notification);
    }

    public static void set(Context context, boolean enabled) {
        StringBuffer buffer = new StringBuffer();
        for (Device device : Device.values()) {
            if ((device == Device.WIFI) && !Prefs.getWifiSetting(context)) {
                buffer.append(device.log + ":- ");
            } else {
                buffer.append(device.log + ":" + (enabled ? "ON" : "OFF") + " ");
                setDeviceState(context, device, enabled);
            }
        }
        Utils.addLog(context, buffer.toString() + "にセットしました");
    }
}