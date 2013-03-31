package com.onedialogproject.galapagosmapho;

import android.content.Context;

import com.onedialogproject.galapagosmapho.DebugTools.Pattern;
import com.onedialogproject.galapagosmapho.ResidentService.Carrier;

public class ResidentServiceScreenOffDataOff extends
        ResidentService.ResidentServiceState {

    public ResidentServiceScreenOffDataOff(Context context,
            ResidentService residentService) {
        super(context, residentService);
    }

    @Override
    public void start() {
        mResidentService.setOff();
        int duration = Prefs.getReconnectDuration(mContext);
        if (Prefs.getDebugMode(mContext)) {
            DebugTools.notify(mContext, Pattern.DATA_OFF);
            duration = 1;// min
        }
        Log.append(mContext, "接続タイマー開始:ネット接続まで" + duration + "分");
        mResidentService.startTimer(duration * 60);
    }

    @Override
    public void end() {
        mResidentService.cancelTimer();
    }

    @Override
    public void onScreenOn() {
        mResidentService.changeState(new ResidentServiceScreenOn(mContext,
                mResidentService));
    }

    @Override
    public void onScreenOff() {
        // Do nothing
    }

    @Override
    public void onCharging() {
        mResidentService.changeState(new ResidentServiceScreenOffCharging(
                mContext, mResidentService));
    }

    @Override
    public void onNotCharging() {
        // Do nothing
    }

    @Override
    public void onNotifyReceiveMail(Carrier carrier) {
        notifyReceiveMail(carrier);
    }

    @Override
    public void onTimerExpired() {
        mResidentService.changeState(new ResidentServiceScreenOffDataOn(
                mContext, mResidentService));
    }

    @Override
    public void onWifiConnected() {
        // Do nothing
    }
}
