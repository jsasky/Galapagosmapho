package com.onedialogproject.galapagosmapho;

import java.util.List;

import com.onedialogproject.galapagosmapho.R;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

public class ResidentService extends Service {

    public static String ACTION_TIMER_EXPIRED = "ACTION_TIMER_EXPIRED";

    public static enum ChargingState {
        UNKNOWN, CHARGING, NOT_CHARGING;
    };

    private static enum DataConnectionState {
        ON, OFF;
    };

    public static abstract class ResidentServiceState {
        protected final Context mContext;
        protected final ResidentService mResidentService;
        protected Handler mHandler = new Handler();

        public ResidentServiceState(Context context,
                ResidentService residentService) {
            mContext = context;
            mResidentService = residentService;
        }

        public abstract void start();

        public abstract void end();

        public abstract void onScreenOn();

        public abstract void onScreenOff();

        public abstract void onCharging();

        public abstract void onNotCharging();

        public abstract void onNotifyReceiveMail();

        public abstract void onTimerExpired();
    };

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                Utils.addLog(context, "画面がONになりました");
                if (Prefs.getMainSetting(context)) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mResidentServiceState.onScreenOn();
                        }
                    });
                } else {
                    Utils.addLog(context, "ガラパゴスマホの設定がOFFのため何もしませんでした");
                }
            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                Utils.addLog(context, "画面がOFFになりました");
                if (Prefs.getMainSetting(context)) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mResidentServiceState.onScreenOff();
                        }
                    });
                } else {
                    Utils.addLog(context, "ガラパゴスマホの設定がOFFのため何もしませんでした");
                }
            } else if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                switch (isCharging(intent, context)) {
                case CHARGING: {
                    if (mChargingState != ChargingState.CHARGING) {
                        Utils.addLog(context, "充電状態:充電中");
                        mChargingState = ChargingState.CHARGING;
                        if (Prefs.getMainSetting(context)) {
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    mResidentServiceState.onCharging();
                                }
                            });
                        } else {
                            Utils.addLog(context, "ガラパゴスマホの設定がOFFのため何もしませんでした");
                        }
                    }
                    break;
                }
                case NOT_CHARGING:
                    if (mChargingState != ChargingState.NOT_CHARGING) {
                        Utils.addLog(context, "充電状態:バッテリー動作中");
                        mChargingState = ChargingState.NOT_CHARGING;
                        if (Prefs.getMainSetting(context)) {
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    mResidentServiceState.onNotCharging();
                                }
                            });
                        } else {
                            Utils.addLog(context, "ガラパゴスマホの設定がOFFのため何もしませんでした");
                        }
                    }
                    break;
                default:
                    break;
                }
            }
        }
    };

    private final NotifyAreaController.Listener mListener = new NotifyAreaController.Listener() {
        @Override
        public void onNotify(String message) {
            Context context = ResidentService.this;
            Utils.addLog(context, "通知:" + message);

            if (!Prefs.getMainSetting(context) || message == null) {
                return;
            }

            for (String string : RECEIVE_MAIL_STRINGS) {
                if (message.equals(string)) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mResidentServiceState.onNotifyReceiveMail();
                        }
                    });
                    break;
                }
            }
        }
    };

    private final IBinder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply,
                int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };

    private static String[] RECEIVE_MAIL_STRINGS;
    private ResidentServiceState mResidentServiceState;
    private ChargingState mChargingState = ChargingState.UNKNOWN;
    private DataConnectionState mDataConnectionState = DataConnectionState.ON;
    private final Handler mHandler = new Handler();
    private PendingIntent mPendingIntent;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mResidentServiceState = new ResidentServiceScreenOn(this, this);
        mResidentServiceState.start();
        RECEIVE_MAIL_STRINGS = getResources().getStringArray(
                R.array.receive_mail_strings);
        mDataConnectionState = DataConnectionState.ON;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_STICKY;
        }

        if (ACTION_TIMER_EXPIRED.equals(intent.getAction())) {
            mResidentServiceState.onTimerExpired();
        } else {
            Context context = ResidentService.this;
            registerReceiver(mBroadcastReceiver, new IntentFilter(
                    Intent.ACTION_SCREEN_ON));
            registerReceiver(mBroadcastReceiver, new IntentFilter(
                    Intent.ACTION_SCREEN_OFF));
            registerReceiver(mBroadcastReceiver, new IntentFilter(
                    Intent.ACTION_BATTERY_CHANGED));

            NotifyAreaController.putNotice(this, R.string.notify_startup,
                    R.string.notify_explanation);
            Utils.addLog(context, "サービスを開始しました");

            NotifyAreaController.setListener(mListener);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Context context = ResidentService.this;
        unregisterReceiver(mBroadcastReceiver);
        Utils.addLog(context, "サービスを停止しました");
        setOn();
        NotifyAreaController.removeNotice(this);
        NotifyAreaController.setListener(null);
    }

    public static boolean isServiceRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> services = activityManager
                .getRunningServices(Integer.MAX_VALUE);

        for (RunningServiceInfo info : services) {
            if (ResidentService.class.getCanonicalName().equals(
                    info.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static ChargingState isCharging(Intent intent, Context context) {
        int status = intent.getIntExtra("status", 0);
        switch (status) {
        case 0:
            // 充電状態不明な場合は充電状態にしてネット接続を継続する
            return ChargingState.CHARGING;
        case BatteryManager.BATTERY_STATUS_CHARGING:
        case BatteryManager.BATTERY_STATUS_FULL:
            return ChargingState.CHARGING;
        case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
            switch (intent.getIntExtra("plugged", 0)) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                return ChargingState.CHARGING;
            case BatteryManager.BATTERY_PLUGGED_USB:
                return ChargingState.CHARGING;
            default:
                break;
            }
            break;
        case BatteryManager.BATTERY_STATUS_DISCHARGING:
        case BatteryManager.BATTERY_STATUS_UNKNOWN:
        default:
            break;
        }

        return ChargingState.NOT_CHARGING;
    }

    public void changeState(ResidentServiceState residentServiceState) {
        mResidentServiceState.end();
        mResidentServiceState = residentServiceState;
        mResidentServiceState.start();
    }

    public ChargingState getChargingState() {
        return mChargingState;
    }

    public boolean setOn() {
        boolean ret = false;
        if (mDataConnectionState != DataConnectionState.ON) {
            Utils.set(this, true);
            mDataConnectionState = DataConnectionState.ON;
            ret = true;
        }
        return ret;
    }

    public boolean setOff() {
        boolean ret = false;
        if (mDataConnectionState != DataConnectionState.OFF) {
            Utils.set(this, false);
            mDataConnectionState = DataConnectionState.OFF;
            ret = true;
        }
        return ret;
    }

    public void startTimer(int time) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (mPendingIntent != null) {
            alarmManager.cancel(mPendingIntent);
        }
        Intent intent = new Intent(this, ResidentService.class);
        intent.setAction(ACTION_TIMER_EXPIRED);
        mPendingIntent = PendingIntent.getService(this, 0, intent, 0);
        long now = System.currentTimeMillis() + 1;
        long expiredTime = now + time * 1000;
        alarmManager.set(AlarmManager.RTC, expiredTime, mPendingIntent);
    }

    public void cancelTimer() {
        if (mPendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(mPendingIntent);
            mPendingIntent = null;
        }
    }
}
