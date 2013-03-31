package com.onedialogproject.galapagosmapho;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class NotifyAreaController extends AccessibilityService {

    public interface Listener {
        public void onNotify(String message);
    }

    private static final Object LISTENER_LOCK = new Object();
    private static Listener mListener = null;

    private static boolean mNotifyCheckFlag = false;
    private static boolean mConfirmed = false;

    public static void setListener(Listener listener) {
        synchronized (LISTENER_LOCK) {
            mListener = listener;
        }
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        final Context context = this;

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.DEFAULT;
        setServiceInfo(info);

        final Handler handler = new Handler();
        mNotifyCheckFlag = false;
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {

                if (Prefs.getMainSetting(context)) {
                    Toast.makeText(context, R.string.startup_message_test,
                            Toast.LENGTH_SHORT).show();
                }

                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (!mNotifyCheckFlag && Prefs.getMainSetting(context)) {
                            Toast.makeText(context,
                                    R.string.startup_message_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, 2000);

            }
        }, 1000);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final Context context = this;

        if (event == null) {
            return;
        }

        int eventType = event.getEventType();
        switch (eventType) {
        case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
            mConfirmed = true;
            if (!mNotifyCheckFlag) {
                if (Prefs.getMainSetting(context)) {
                    Toast.makeText(context, R.string.startup_message_success,
                            Toast.LENGTH_SHORT).show();
                }
                mNotifyCheckFlag = true;
            }

            String message = String.valueOf(event.getText());
            if (message == null || message.trim().equals("")
                    || message.trim().equals("[]")) {
                return;
            }

            synchronized (LISTENER_LOCK) {
                if (mListener != null) {
                    mListener.onNotify(message);
                }
            }
            break;
        default:
            break;
        }
    }

    @Override
    public void onInterrupt() {
        // Do nothing
    }

    public static boolean isActivated(Context context) {
        return (mConfirmed && Utils.isNotifyAreaControllerAvailable(context));
    }

    public static final int NOTIFICATION_ID = 1119;

    private static Notification createNotification(Context context, int id,
            int id_exp) {
        Notification notification = new Notification(R.drawable.ic_stat_notify,
                context.getString(id), System.currentTimeMillis());

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, Setting.class), 0);
        notification.setLatestEventInfo(context,
                context.getString(R.string.app_name),
                context.getString(id_exp), pendingIntent);

        notification.flags = notification.flags | Notification.FLAG_NO_CLEAR
                | Notification.FLAG_ONGOING_EVENT;
        notification.number = 0;
        return notification;
    }

    public static void putNotice(Context context, int id, int id_exp) {
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = createNotification(context, id, id_exp);
        nm.notify(NotifyAreaController.NOTIFICATION_ID, notification);
    }

    public static void removeNotice(Context context) {
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);
    }
}
