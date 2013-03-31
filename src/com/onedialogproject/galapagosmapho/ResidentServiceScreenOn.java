package com.onedialogproject.galapagosmapho;

import com.onedialogproject.galapagosmapho.DebugTools.Pattern;
import com.onedialogproject.galapagosmapho.ResidentService.Carrier;
import com.onedialogproject.galapagosmapho.ResidentService.ChargingState;
import com.onedialogproject.galapagosmapho.Utils.Device;

public class ResidentServiceScreenOn extends
        ResidentService.ResidentServiceState {

    private static final int WIFI_OFF_DURATION = 60;// sec

    public ResidentServiceScreenOn(ResidentService residentService) {
        super(residentService);
    }

    @Override
    public void start() {
        if (Prefs.getDebugMode(mContext)) {
            DebugTools.notify(mContext, Pattern.SCREEN_ON);
        }

        if (mResidentService.setOn()) {
            NotifyAreaController.putNotice(mContext, R.string.notify_connected,
                    R.string.notify_explanation);
        }

        if (Prefs.getWifiSetting(mContext)) {
            if (mResidentService.getChargingState() == ChargingState.CHARGING) {
                Log.append(mContext, "WiFiタイマー作動なし:充電中");
            } else if (mResidentService.isWifiConnected()) {
                Log.append(mContext, "WiFiタイマー作動なし:WiFi接続済み");
            } else {
                mResidentService.startTimer(WIFI_OFF_DURATION);
                Utils.setDeviceState(mContext, Device.WIFI, true);
                Log.append(mContext, "WiFiタイマー開始:OFFまで" + WIFI_OFF_DURATION
                        + "秒");
            }
        }
    }

    @Override
    public void end() {
        mResidentService.cancelTimer();
    }

    @Override
    public void onScreenOn() {
        // Fail safe
        Utils.set(mContext, true);
    }

    @Override
    public void onScreenOff() {
        if (mResidentService.getChargingState() == ChargingState.CHARGING) {
            mResidentService.changeState(new ResidentServiceScreenOffCharging(
                    mResidentService));
        } else {
            mResidentService.changeState(new ResidentServiceScreenOffDataOn(
                    mResidentService));
        }
    }

    @Override
    public void onCharging() {
        if (Prefs.getWifiSetting(mContext)) {
            Log.append(mContext, "WiFiタイマー停止:充電開始/WiFi ON");
            Utils.setDeviceState(mContext, Device.WIFI, true);
            mResidentService.cancelTimer();
        }
    }

    @Override
    public void onNotCharging() {
        if (Prefs.getWifiSetting(mContext)) {
            if (mResidentService.isWifiConnected()) {
                Log.append(mContext, "WiFiタイマー作動なし:WiFi接続済み");
            } else {
                mResidentService.startTimer(WIFI_OFF_DURATION);
                Utils.setDeviceState(mContext, Device.WIFI, true);
                Log.append(mContext, "WiFiタイマー開始:OFFまで" + WIFI_OFF_DURATION
                        + "秒");
            }
        }
    }

    @Override
    public void onNotifyReceiveMail(Carrier carrier) {
        // Do nothing
    }

    @Override
    public void onTimerExpired() {
        if (Prefs.getWifiSetting(mContext)
                && !mResidentService.isWifiConnected()) {
            Log.append(mContext, "WiFiタイマー満了:Wi-FiをOFFにしました");
            Utils.setDeviceState(mContext, Device.WIFI, false);
        }
    }

    @Override
    public void onWifiConnected() {
        if (Prefs.getWifiSetting(mContext)) {
            Log.append(mContext, "WiFiタイマー停止:WiFi接続を検知");
            mResidentService.cancelTimer();
        }
    }
}
