package com.onedialogproject.galapagosmapho;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

public class ResidentService extends Service {

    public static String ACTION_TIMER_EXPIRED = "ACTION_TIMER_EXPIRED";

    public static enum ServiceState {
        SCREEN_ON(0), SCREEN_OFF_CHARGING(1), SCREEN_OFF_DATA_ON(2), SCREEN_OFF_DATA_OFF(
                3), SCREEN_OFF_TETHERING(4);

        private final int mId;

        ServiceState(int id) {
            mId = id;
        }

        public int getId() {
            return mId;
        }

        public static ServiceState getState(int id) {
            for (ServiceState serviceState : ServiceState.values()) {
                if (serviceState.getId() == id) {
                    return serviceState;
                }
            }
            return ServiceState.SCREEN_ON;
        }
    };

    public static enum ChargingState {
        UNKNOWN, CHARGING, NOT_CHARGING;
    };

    public static enum Carrier {
        DOCOMO, AU, SOFTBANK, UNKNOWN,
    };

    private static enum DataConnectionState {
        ON, OFF;
    };

    public static abstract class ResidentServiceState {
        protected final Context mContext;
        protected final ResidentService mResidentService;
        protected Handler mHandler = new Handler();

        public ResidentServiceState(ResidentService residentService) {
            mContext = residentService;
            mResidentService = residentService;
        }

        public abstract void start();

        public abstract void end();

        public abstract ServiceState getState();

        public abstract void onScreenOn();

        public abstract void onScreenOff();

        public abstract void onCharging();

        public abstract void onNotCharging();

        public abstract void onNotifyReceiveMail(Carrier carrier);

        public abstract void onTimerExpired();

        public abstract void onWifiConnected();

        public void notifyReceiveMail(Carrier carrier) {
            switch (carrier) {
            case DOCOMO:
                Log.append(mContext, "docomoメール着信");
                Utils.notify(mContext);
            case AU:
                Log.append(mContext, "auメール着信");
                // Do nothing
                break;
            case SOFTBANK:
                Log.append(mContext, "SoftBankメール着信");
                // Do nothing
                break;
            case UNKNOWN:
            default:
                // Do nothing
                break;
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, Intent intent) {
            if ((intent == null) || (context == null)) {
                return;
            }

            String action = intent.getAction();
            if (action == null) {
                return;
            }

            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                screenOn(context);
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                screenOff(context);
            } else if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                switch (isCharging(intent, context)) {
                case CHARGING: {
                    if (mChargingState != ChargingState.CHARGING) {
                        onCharging(context);
                    }
                    break;
                }
                case NOT_CHARGING:
                    if (mChargingState != ChargingState.NOT_CHARGING) {
                        onNotCharging(context);
                    }
                    break;
                default:
                    break;
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                onNetworkStateChanged(context, intent);
            } else if ((intent != null) && ACTION_TIMER_EXPIRED.equals(action)) {
                onTimerExpired(context);
            }
        }

        private void screenOn(final Context context) {
            Log.append(context, "通知:画面ON");
            if (!Prefs.isActivated(context) && !Prefs.getWifiSetting(context)) {
                Log.append(context, "ガラパゴスマホの設定がOFFのため何もしませんでした");
                return;
            }
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    try {
                        mResidentServiceState.onScreenOn();
                    } catch (Throwable e) {
                        Log.printStackTrace(context, "エラー発生:onScreenOn", e);
                    }
                }
            });
        }

        private void screenOff(final Context context) {
            Log.append(context, "通知:画面OFF");
            if (!Prefs.isActivated(context) && !Prefs.getWifiSetting(context)) {
                Log.append(context, "ガラパゴスマホの設定がOFFのため何もしませんでした");
                return;
            }
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    try {
                        mResidentServiceState.onScreenOff();
                    } catch (Throwable e) {
                        Log.printStackTrace(context, "エラー発生:onScreenOff", e);
                    }
                }
            });
        }

        private void onCharging(final Context context) {
            Log.append(context, "通知:充電開始");
            mChargingState = ChargingState.CHARGING;
            if (!Prefs.isActivated(context)) {
                Log.append(context, "ガラパゴスマホの設定がOFFのため何もしませんでした");
                return;
            }
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    try {
                        mResidentServiceState.onCharging();
                    } catch (Throwable e) {
                        Log.printStackTrace(context, "エラー発生:onCharging", e);
                    }
                }
            });
        }

        private void onNotCharging(final Context context) {
            Log.append(context, "通知:バッテリー動作開始");
            mChargingState = ChargingState.NOT_CHARGING;
            if (!Prefs.isActivated(context)) {
                Log.append(context, "ガラパゴスマホの設定がOFFのため何もしませんでした");
                return;
            }

            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    try {
                        mResidentServiceState.onNotCharging();
                    } catch (Throwable e) {
                        Log.printStackTrace(context, "エラー発生:onNotCharging", e);
                    }
                }
            });
        }

        private void onNetworkStateChanged(final Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras == null) {
                return;
            }
            NetworkInfo networkInfo = extras
                    .getParcelable(WifiManager.EXTRA_NETWORK_INFO);
            if (networkInfo == null) {
                return;
            }
            if (!networkInfo.isConnected()) {
                return;
            }

            Log.append(context, "通知:WiFi接続");
            if (!Prefs.isActivated(context)) {
                Log.append(context, "ガラパゴスマホの設定がOFFのため何もしませんでした");
                return;
            }
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    try {
                        mResidentServiceState.onWifiConnected();
                    } catch (Throwable e) {
                        Log.printStackTrace(context, "エラー発生:onWifiConnected", e);
                    }
                }
            });
        }

        private void onTimerExpired(final Context context) {
            Log.append(context, "通知:タイマー満了");
            if (!Prefs.isActivated(context)) {
                Log.append(context, "ガラパゴスマホの設定がOFFのため何もしませんでした");
                return;
            }

            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    try {
                        mPendingIntent = null;
                        mResidentServiceState.onTimerExpired();
                    } catch (Throwable e) {
                        Log.printStackTrace(context, "エラー発生:onTimerExpired", e);
                    }
                }
            });

        }
    };

    private final NotifyAreaController.Listener mListener = new NotifyAreaController.Listener() {
        @Override
        public void onNotify(String message) {
            Context context = ResidentService.this;

            if (message == null) {
                return;
            }

            final Carrier carrier;
            if (message.equals("[未受信メール]") || message.equals("[未受信メールがあります]")) {
                Log.append(context, "通知:" + message);
                carrier = Carrier.DOCOMO;
            } else if (message.equals("[メール着信通知]")) {
                Log.append(context, "通知:" + message);
                carrier = Carrier.AU;
            } else if (message.startsWith("[SMS受信：")) {
                Log.append(context, "通知:" + message);
                carrier = Carrier.SOFTBANK;
            } else {
                return;
            }

            if (!Prefs.isActivated(context)) {
                Log.append(context, "ガラパゴスマホの設定がOFFのため何もしませんでした");
                return;
            }

            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mResidentServiceState.onNotifyReceiveMail(carrier);
                }
            });
        }
    };

    private final IBinder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply,
                int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };

    private ResidentServiceState mResidentServiceState;
    private ChargingState mChargingState = ChargingState.UNKNOWN;
    private DataConnectionState mDataConnectionState = DataConnectionState.ON;
    private final Handler mHandler = new Handler();
    private PendingIntent mPendingIntent;
    private ConnectivityManager mConnectivityManager;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDataConnectionState = DataConnectionState.ON;
        mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Context context = this;
        if (Prefs.getActiveFlag(context)) {
            Log.append(context, "OSによるサービス再起動");
        } else {
            Log.append(context, "サービスを開始しました");
            NotifyAreaController.putNotice(context, R.string.notify_startup,
                    R.string.notify_explanation);
        }
        Prefs.setActiveFlag(context, true);
        NotifyAreaController.setListener(mListener);
        registerReceiver(mBroadcastReceiver, new IntentFilter(
                Intent.ACTION_SCREEN_ON));
        registerReceiver(mBroadcastReceiver, new IntentFilter(
                Intent.ACTION_SCREEN_OFF));
        registerReceiver(mBroadcastReceiver, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));
        registerReceiver(mBroadcastReceiver, new IntentFilter(
                WifiManager.NETWORK_STATE_CHANGED_ACTION));
        registerReceiver(mBroadcastReceiver, new IntentFilter(
                ACTION_TIMER_EXPIRED));

        switch (Prefs.getServiceState(context)) {
        case SCREEN_OFF_TETHERING:
            mResidentServiceState = new ResidentServiceScreenOffTethering(this);
            break;
        case SCREEN_OFF_CHARGING:
            mResidentServiceState = new ResidentServiceScreenOffCharging(this);
            break;
        case SCREEN_OFF_DATA_ON:
            mResidentServiceState = new ResidentServiceScreenOffDataOn(this);
            break;
        case SCREEN_OFF_DATA_OFF:
            mResidentServiceState = new ResidentServiceScreenOffDataOff(this);
            break;
        case SCREEN_ON:
        default:
            mResidentServiceState = new ResidentServiceScreenOn(this);
            break;
        }
        mResidentServiceState.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Context context = ResidentService.this;
        setOn();
        NotifyAreaController.removeNotice(this);
        Log.append(context, "サービス停止");
        Prefs.setActiveFlag(context, false);
        unregisterReceiver(mBroadcastReceiver);
        mResidentServiceState.end();
    }

    public void changeState(ResidentServiceState residentServiceState) {
        Context context = this;
        mResidentServiceState.end();
        mResidentServiceState = residentServiceState;
        Prefs.setServiceState(context, mResidentServiceState.getState());
        mResidentServiceState.start();
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

    public ChargingState getChargingState() {
        return mChargingState;
    }

    public boolean setOn() {
        if (mDataConnectionState == DataConnectionState.ON) {
            return false;
        }
        Log.setDevice(this, true);
        Utils.set(this, true);
        mDataConnectionState = DataConnectionState.ON;
        return true;
    }

    public boolean setOff() {
        if (mDataConnectionState == DataConnectionState.OFF) {
            return false;
        }
        Log.setDevice(this, false);
        Utils.set(this, false);
        mDataConnectionState = DataConnectionState.OFF;
        return true;
    }

    public boolean isWifiConnected() {
        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();

        if (networkInfo == null) {
            return false;
        }

        if (!networkInfo.isConnected()) {
            return false;
        }

        if (!networkInfo.getTypeName().equals("WIFI")) {
            return false;
        }

        return true;
    }

    public void startTimer(int time) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (mPendingIntent != null) {
            alarmManager.cancel(mPendingIntent);
        }
        Intent intent = new Intent();
        intent.setAction(ACTION_TIMER_EXPIRED);
        mPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
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
