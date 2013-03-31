package com.onedialogproject.galapagosmapho;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Assert;
import android.app.Notification;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;

public class Utils {

    public static enum Device {
        WIFI("WiFi"), DATA_3G_LTE("3G"), BLUETOOTH("BT");

        final String log;

        Device(String log) {
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
        case BLUETOOTH: {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                    .getDefaultAdapter();
            if (bluetoothAdapter == null) {
                Log.append(context, "エラー:BTの設定を変更できません");
            } else {
                if (enabled) {
                    bluetoothAdapter.enable();
                } else {
                    bluetoothAdapter.disable();
                }
            }
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
        case BLUETOOTH: {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                    .getDefaultAdapter();
            if (bluetoothAdapter == null) {
                Log.append(context, "エラー:BTの設定を取得できません");
                return false;
            }
            return bluetoothAdapter.isEnabled();
        }
        default:
            Assert.assertTrue(false);
            break;
        }
        return false;
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
        for (Device device : Device.values()) {
            switch (device) {
            case WIFI:
                if (Prefs.getWifiSetting(context)) {
                    setDeviceState(context, Device.WIFI, enabled);
                }
                break;
            case DATA_3G_LTE:
                if (Prefs.getLte3gSetting(context)) {
                    setDeviceState(context, Device.DATA_3G_LTE, enabled);
                }
            case BLUETOOTH:
                if (Prefs.getBluetoothSetting(context)) {
                    setDeviceState(context, Device.BLUETOOTH, enabled);
                }
            default:
                break;
            }
        }
    }

    public static boolean getWifiTetheringState(Context context) {
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);

        boolean ret = false;
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApState");
            Integer result = (Integer) method.invoke(wifiManager);
            if (result != null) {
                if (result > 10) {
                    result -= 10;
                }
                final int WIFI_AP_STATE_ENABLING = 2;
                final int WIFI_AP_STATE_ENABLED = 3;
                switch (result) {
                case WIFI_AP_STATE_ENABLING:
                case WIFI_AP_STATE_ENABLED:
                    ret = true;
                    break;
                default:
                    break;
                }
            }
        } catch (Exception e) {
            // Do nothing
        }

        return ret;
    }
}