package com.onedialogproject.galapagosmapho;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;

import com.onedialogproject.galapagosmapho.Utils.Device;

public class Log {

    private static final int MAX_LOG_SIZE = 10000;

    private static boolean sInitialized = false;
    private static String LOG_FILE_PATH = "Galapagosmapho_log.txt";
    private static StringBuffer sStringBuffer = null;

    public static void initialze(Context context) {
        if (sInitialized) {
            return;
        }

        sInitialized = true;
        sStringBuffer = new StringBuffer(MAX_LOG_SIZE);
        BufferedInputStream bufferedInputStream = null;
        try {
            bufferedInputStream = new BufferedInputStream(
                    context.openFileInput(LOG_FILE_PATH));
            byte[] readBytes = new byte[bufferedInputStream.available()];
            bufferedInputStream.read(readBytes);
            String readString = new String(readBytes);
            sStringBuffer.append(readString);
        } catch (FileNotFoundException e1) {
            // Do nothing
        } catch (IOException e) {
            // Do nothing
        } finally {
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    // Give up
                }
            }
        }
    }

    public static String read(Context context) {
        if (!sInitialized) {
            initialze(context);
        }
        return sStringBuffer.toString().trim();
    }

    public static void write(Context context, String string) {
        if (!sInitialized) {
            initialze(context);
        }

        sStringBuffer.insert(0, string);
        sStringBuffer.append("\n");
        if (sStringBuffer.length() > MAX_LOG_SIZE) {
            sStringBuffer.delete(MAX_LOG_SIZE, sStringBuffer.length());
        }

        BufferedOutputStream bufferedOutputStream = null;
        try {
            bufferedOutputStream = new BufferedOutputStream(
                    context.openFileOutput(LOG_FILE_PATH, Context.MODE_PRIVATE));
            bufferedOutputStream.write(sStringBuffer.toString().trim()
                    .getBytes());
            bufferedOutputStream.flush();
        } catch (FileNotFoundException e) {
            // Do nothing
        } catch (IOException e) {
            // Do nothing
        } finally {
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    // Give up
                }
            }
        }
    }

    public static void clear(Context context) {
        sStringBuffer.delete(0, sStringBuffer.length());

        BufferedOutputStream bufferedOutputStream = null;
        try {
            bufferedOutputStream = new BufferedOutputStream(
                    context.openFileOutput(LOG_FILE_PATH, Context.MODE_PRIVATE));
            bufferedOutputStream.write("".getBytes());
            bufferedOutputStream.flush();
        } catch (FileNotFoundException e) {
            // Do nothing
        } catch (IOException e) {
            // Do nothing
        } finally {
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    // Give up
                }
            }
        }
    }

    public static void setDevice(Context context, boolean enabled) {
        StringBuffer buffer = new StringBuffer();
        for (Device device : Device.values()) {
            switch (device) {
            case WIFI:
                if (Prefs.getWifiSetting(context)) {
                    buffer.append(device.log + ":" + (enabled ? "ON" : "OFF")
                            + " ");
                }
                break;
            case DATA_3G_LTE:
                if (Prefs.getLte3gSetting(context)) {
                    buffer.append(device.log + ":" + (enabled ? "ON" : "OFF")
                            + " ");
                }
                break;
            case BLUETOOTH:
                if (Prefs.getBluetoothSetting(context)) {
                    buffer.append(device.log + ":" + (enabled ? "ON" : "OFF")
                            + " ");
                }
                break;
            }
        }
        buffer.append("にセット");
        append(context, buffer.toString().trim());
    }

    public static void append(Context context, String line) {
        Log.write(context, getDateTimeString(context) + " " + line + "\n");
        DebugTools.routeCheck(line, true);
    }

    private static String getDateTimeString(Context context) {
        final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat(
                "MM/dd kk:mm:ss");
        Date date = new Date();
        return DATETIME_FORMAT.format(date);
    }

    public static void printStackTrace(Context context, String message,
            Throwable e) {
        StringBuffer output = new StringBuffer();
        output.append(message);
        output.append("[");
        output.append(e.getClass().getName());
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        if (stackTraceElements != null) {
            output.append(" at ");
            output.append(stackTraceElements[0].getFileName());
            output.append("(line ");
            output.append(stackTraceElements[0].getLineNumber());
            output.append(" in ");
            output.append(stackTraceElements[0].getMethodName());
            output.append(")");
            output.append("]");
        }
        Log.write(context, getDateTimeString(context) + " " + output + "\n");
        DebugTools.routeCheck(output.toString(), true);
    }
}
