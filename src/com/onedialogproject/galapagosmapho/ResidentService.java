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
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                Log.append(context, "画面がONになりました");
                if (Prefs.getMainSetting(context)) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                mResidentServiceState.onScreenOn();
                            } catch (Throwable e) {
                                Log.printStackTrace(context,
                                        "エラー発生:onScreenOn", e);
                            }
                        }
                    });
                } else {
                    Log.append(context, "ガラパゴスマホの設定がOFFのため何もしませんでした");
                }
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                Log.append(context, "画面がOFFになりました");
                if (Prefs.getMainSetting(context)) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                mResidentServiceState.onScreenOff();
                            } catch (Throwable e) {
                                Log.printStackTrace(context,
                                        "エラー発生:onScreenOff", e);
                            }
                        }
                    });
                } else {
                    Log.append(context, "ガラパゴスマホの設定がOFFのため何もしませんでした");
                }
            } else if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                switch (isCharging(intent, context)) {
                case CHARGING: {
                    if (mChargingState != ChargingState.CHARGING) {
                        Log.append(context, "充電が開始されました");
                        mChargingState = ChargingState.CHARGING;
                        if (Prefs.getMainSetting(context)) {
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        mResidentServiceState.onCharging();
                                    } catch (Throwable e) {
                                        Log.printStackTrace(context,
                                                "エラー発生:onCharging", e);
                                    }
                                }
                            });
                        } else {
                            Log.append(context, "ガラパゴスマホの設定がOFFのため何もしませんでした");
                        }
                    }
                    break;
                }
                case NOT_CHARGING:
                    if (mChargingState != ChargingState.NOT_CHARGING) {
                        Log.append(context, "バッテリー動作を開始しました");
                        mChargingState = ChargingState.NOT_CHARGING;
                        if (Prefs.getMainSetting(context)) {
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        mResidentServiceState.onNotCharging();
                                    } catch (Throwable e) {
                                        Log.printStackTrace(context,
                                                "エラー発生:onNotCharging", e);
                                    }
                                }
                            });
                        } else {
                            Log.append(context, "ガラパゴスマホの設定がOFFのため何もしませんでした");
                        }
                    }
                    break;
                default:
                    break;
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    NetworkInfo networkInfo = extras
                            .getParcelable(WifiManager.EXTRA_NETWORK_INFO);
                    if (networkInfo != null) {
                        if (networkInfo.isConnected()) {
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        mResidentServiceState.onWifiConnected();
                                    } catch (Throwable e) {
                                        Log.printStackTrace(context,
                                                "エラー発生:onWifiConnected", e);
                                    }
                                }
                            });
                        }
                    }
                }
            } else if ((intent != null)
                    && ACTION_TIMER_EXPIRED.equals(intent.getAction())) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            mPendingIntent = null;
                            mResidentServiceState.onTimerExpired();
                        } catch (Throwable e) {
                            Log.printStackTrace(context,
                                    "エラー発生:onTimerExpired", e);
                        }
                    }
                });
            }
        }
    };

    private final NotifyAreaController.Listener mListener = new NotifyAreaController.Listener() {
        @Override
        public void onNotify(String message) {
            Context context = ResidentService.this;
            Log.append(context, "通知:" + message);

            if (!Prefs.getMainSetting(context) || message == null) {
                return;
            }

            final Carrier carrier;
            if (message.equals("[未受信メール]") || message.equals("[未受信メールがあります]")) {
                carrier = Carrier.DOCOMO;
            } else if (message.equals("[メール着信通知]")) {
                carrier = Carrier.AU;
            } else if (message.startsWith("[SMS受信：")) {
                carrier = Carrier.SOFTBANK;
            } else {
                carrier = Carrier.UNKNOWN;
            }

            if (carrier != null) {
                final Carrier carrierNotify = carrier;
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        mResidentServiceState
                                .onNotifyReceiveMail(carrierNotify);
                    }
                });
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

    private ResidentServiceState mResidentServiceState;
    private ChargingState mChargingState = ChargingState.UNKNOWN;
    private DataConnectionState mDataConnectionState = DataConnectionState.ON;
    private final Handler mHandler = new Handler();
    private PendingIntent mPendingIntent;
    private ConnectivityManager mConnectivityManager;
    private static boolean mActiveFlag = false;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDataConnectionState = DataConnectionState.ON;
        mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        mResidentServiceState = new ResidentServiceScreenOn(this);
        mResidentServiceState.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Context context = this;
        if (mActiveFlag) {
            Log.append(context, "OSによりサービスが再起動されました");
        } else {
            Log.append(context, "サービスが開始されました");
            NotifyAreaController.putNotice(context, R.string.notify_startup,
                    R.string.notify_explanation);
        }
        mActiveFlag = true;
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

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Context context = ResidentService.this;
        setOn();
        NotifyAreaController.removeNotice(this);
        Log.append(context, "サービスが停止しました");
        mActiveFlag = false;
        unregisterReceiver(mBroadcastReceiver);
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
            Log.setDevice(this, true);
            Utils.set(this, true);
            mDataConnectionState = DataConnectionState.ON;
            ret = true;
        }
        return ret;
    }

    public boolean setOff() {
        boolean ret = false;
        if (mDataConnectionState != DataConnectionState.OFF) {
            Log.setDevice(this, false);
            Utils.set(this, false);
            mDataConnectionState = DataConnectionState.OFF;
            ret = true;
        }
        return ret;
    }

    public boolean isWifiConnected() {
        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();

        if (networkInfo != null) {
            if (networkInfo.isConnected()) {
                if (networkInfo.getTypeName().equals("WIFI")) {
                    return true;
                }
            }
        }
        return false;
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
