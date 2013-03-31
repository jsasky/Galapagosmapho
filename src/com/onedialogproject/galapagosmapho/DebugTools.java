package com.onedialogproject.galapagosmapho;

import com.onedialogproject.galapagosmapho.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

public class DebugTools {
    final static String TAG_PREFIX = "Galapagosmapho";

    static enum Pattern {
        SCREEN_ON, CHARGING, NOT_CHARGING, DATA_OFF, DATA_ON, VIBRATE,
    }

    public static void notify(Context context, Pattern pattern) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        final Notification notification = new Notification();

        switch (pattern) {
        case SCREEN_ON: {
            // Do nothing
            break;
        }
        case DATA_OFF: {
            long[] vibrate = { 100, 100, 100, 100, 100, 100, 100, 100, 100 };
            notification.vibrate = vibrate;
            break;
        }
        case DATA_ON: {
            long[] vibrate = { 100, 100, 100, 100, 100, 100, 100, 100, 100 };
            notification.vibrate = vibrate;
            notification.ledARGB = 0xff0000ff;
            notification.ledOnMS = 100;
            notification.ledOffMS = 100;
            notification.flags |= Notification.FLAG_SHOW_LIGHTS;
            break;
        }
        case CHARGING: {
            long[] vibrate = { 100, 100, 100, 100, 100, 100, 100, 100, 100 };
            notification.vibrate = vibrate;
            notification.ledARGB = 0xffff00ff;
            notification.ledOnMS = 100;
            notification.ledOffMS = 100;
            notification.flags |= Notification.FLAG_SHOW_LIGHTS;
            break;
        }
        case NOT_CHARGING: {
            long[] vibrate = { 100, 100, 100, 100, 100, 100, 100, 100, 100 };
            notification.vibrate = vibrate;
            notification.ledARGB = 0xffffff00;
            notification.ledOnMS = 100;
            notification.ledOffMS = 100;
            notification.flags |= Notification.FLAG_SHOW_LIGHTS;
            break;
        }
        case VIBRATE: {
            long[] vibrate = { 100 };
            notification.vibrate = vibrate;
        }
        default: {
            notification.defaults = Notification.DEFAULT_VIBRATE;
            break;
        }
        }

        notificationManager.notify(R.string.app_name, notification);
    }

    public static void printStackTrace() {
        java.lang.Exception exception = new java.lang.Exception();
        printStackTrace(exception);
    }

    public static void printStackTrace(Exception e) {
        String msg = e.getLocalizedMessage();
        String name = e.getClass().getName();
        if (msg == null) {
            Log.e(TAG_PREFIX, name);
        } else {
            Log.e(TAG_PREFIX, name + ": " + msg);
        }

        StackTraceElement[] stack = e.getStackTrace();
        if (stack != null) {
            for (StackTraceElement element : stack) {
                Log.e(TAG_PREFIX, "\tat " + element);
            }
        }

        StackTraceElement[] parentStack = stack;
        Throwable throwable = e.getCause();
        while (throwable != null) {
            Log.e(TAG_PREFIX, "Caused by: ");
            Log.e(TAG_PREFIX, throwable.getMessage());
            StackTraceElement[] currentStack = throwable.getStackTrace();
            int duplicates = countDuplicates(currentStack, parentStack);
            for (int i = 0; i < currentStack.length - duplicates; i++) {
                Log.e(TAG_PREFIX, "\tat " + currentStack[i]);
            }
            if (duplicates > 0) {
                Log.e(TAG_PREFIX, "\t... " + duplicates + " more");
            }
            parentStack = currentStack;
            throwable = throwable.getCause();
        }
    }

    private static int countDuplicates(StackTraceElement[] currentStack,
            StackTraceElement[] parentStack) {
        int duplicates = 0;
        int parentIndex = parentStack.length;
        for (int i = currentStack.length; --i >= 0 && --parentIndex >= 0;) {
            StackTraceElement parentFrame = parentStack[parentIndex];
            if (parentFrame.equals(currentStack[i])) {
                duplicates++;
            } else {
                break;
            }
        }
        return duplicates;
    }

    public static void routeCheck() {
        java.lang.Exception exception = new java.lang.Exception();
        StackTraceElement stackTraceElement[] = exception.getStackTrace();

        String classNameWithPackage = stackTraceElement[1].getClassName();
        int pos = classNameWithPackage.lastIndexOf(".");
        String className = classNameWithPackage.substring(pos + 1);
        String message = className + "#" + stackTraceElement[1].getMethodName();

        log(true, message, stackTraceElement[1]);
    }

    public static void routeCheck(String message) {
        routeCheck(message, true);
    }

    public static void routeCheck(String message, boolean logoutput) {
        java.lang.Exception exception = new java.lang.Exception();
        StackTraceElement stackTraceElement[] = exception.getStackTrace();

        int stack = logoutput ? 2 : 1;
        String classNameWithPackage = stackTraceElement[stack].getClassName();
        int pos = classNameWithPackage.lastIndexOf(".");
        String className = classNameWithPackage.substring(pos + 1);
        String output = className + "#"
                + stackTraceElement[stack].getMethodName();

        output += "[" + message + "]";

        log(true, output, stackTraceElement[stack]);
    }

    public static void log(boolean condition, String message) {
        java.lang.Exception exception = new java.lang.Exception();
        StackTraceElement stackTraceElement[] = exception.getStackTrace();

        log(condition, message, stackTraceElement[1]);
    }

    public static void log(String message) {
        java.lang.Exception exception = new java.lang.Exception();
        StackTraceElement stackTraceElement[] = exception.getStackTrace();

        log(true, message, stackTraceElement[1]);
    }

    private static void log(boolean condition, String message,
            StackTraceElement stackTraceElement) {
        if (condition == false) {
            return;
        }

        Log.d(TAG_PREFIX,
                message + " at " + stackTraceElement.getClassName() + "."
                        + stackTraceElement.getMethodName() + "("
                        + stackTraceElement.getFileName() + ":"
                        + stackTraceElement.getLineNumber() + ")");
    }

    static private long mTime[] = new long[10];
    static private long mLastCheckTime[] = new long[10];

    static public void setStartTime(int index) {
        mTime[index] = SystemClock.uptimeMillis();
        mLastCheckTime[index] = mTime[index];
    }

    static public void printTime(int index) {
        long time = SystemClock.uptimeMillis();
        java.lang.Exception exception = new java.lang.Exception();
        StackTraceElement stackTraceElement[] = exception.getStackTrace();

        String classNameWithPackage = stackTraceElement[1].getClassName();
        int pos = classNameWithPackage.lastIndexOf(".");
        String className = classNameWithPackage.substring(pos + 1);
        String output = "Time" + index + " = "
                + ((time - mLastCheckTime[index]) / 1000f) + "ms (Total:"
                + ((time - mTime[index]) / 1000f) + "ms)," + className + "#"
                + stackTraceElement[1].getMethodName();

        log(true, output, stackTraceElement[1]);
        mLastCheckTime[index] = time;
    }

    static public void printTime(int index, String message) {
        long time = SystemClock.uptimeMillis();
        java.lang.Exception exception = new java.lang.Exception();
        StackTraceElement stackTraceElement[] = exception.getStackTrace();

        String classNameWithPackage = stackTraceElement[1].getClassName();
        int pos = classNameWithPackage.lastIndexOf(".");
        String className = classNameWithPackage.substring(pos + 1);
        String output = "Time" + index + " = "
                + ((time - mLastCheckTime[index]) / 1000f) + "ms (Total:"
                + ((time - mTime[index]) / 1000f) + "ms)," + className + "#"
                + stackTraceElement[1].getMethodName();

        output += "[" + message + "]";

        log(true, output, stackTraceElement[1]);

        mLastCheckTime[index] = time;
    }

    public static void stackTraceCheck() {
        java.lang.Exception exception = new java.lang.Exception();
        StackTraceElement stackTraceElement[] = exception.getStackTrace();

        String classNameWithPackage = stackTraceElement[1].getClassName();
        int pos = classNameWithPackage.lastIndexOf(".");
        String className = classNameWithPackage.substring(pos + 1);
        String output = "TRACE CHECK:" + className + "#"
                + stackTraceElement[1].getMethodName();

        final StringBuilder stackTraceBuilder = new StringBuilder();
        final int initialIndex = 1;
        for (int i = initialIndex; i < stackTraceElement.length; i++) {
            stackTraceBuilder.append("\n");
            if (i != initialIndex) {
                stackTraceBuilder.append("    ");
            }
            stackTraceBuilder.append(stackTraceElement[i].toString());
        }
        stackTraceBuilder.append("\n");
        output += stackTraceBuilder.toString();

        log(true, output, stackTraceElement[1]);
    }

    public static void stackTraceCheck(final String message) {
        java.lang.Exception exception = new java.lang.Exception();
        StackTraceElement stackTraceElement[] = exception.getStackTrace();

        String classNameWithPackage = stackTraceElement[1].getClassName();
        int pos = classNameWithPackage.lastIndexOf(".");
        String className = classNameWithPackage.substring(pos + 1);
        String output = "TRACE CHECK:" + className + "#"
                + stackTraceElement[1].getMethodName();

        if (!TextUtils.isEmpty(message)) {
            output += "[" + message + "]";
        }

        final StringBuilder stackTraceBuilder = new StringBuilder();
        final int initialIndex = 1;
        for (int i = initialIndex; i < stackTraceElement.length; i++) {
            stackTraceBuilder.append("\n");
            if (i != initialIndex) {
                stackTraceBuilder.append("    ");
            }
            stackTraceBuilder.append(stackTraceElement[i].toString());
        }
        stackTraceBuilder.append("\n");
        output += stackTraceBuilder.toString();

        log(true, output, stackTraceElement[1]);
    }
}
