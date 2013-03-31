package com.onedialogproject.galapagosmapho;

import android.content.Context;

import com.onedialogproject.galapagosmapho.DebugTools.Pattern;
import com.onedialogproject.galapagosmapho.ResidentService.Carrier;

public class ResidentServiceScreenOffDataOnFirst extends
        ResidentService.ResidentServiceState {

    private static final int DATA_OFF_DURATION = 10;// sec

    public ResidentServiceScreenOffDataOnFirst(Context context,
            ResidentService residentService) {
        super(context, residentService);
    }

    @Override
    public void start() {
        mResidentService.setOn();
        if (Prefs.getDebugMode(mContext)) {
            DebugTools.notify(mContext, Pattern.DATA_ON);
        }
        Log.append(mContext, "切断タイマー開始:ネット切断まで" + DATA_OFF_DURATION + "秒");
        mResidentService.startTimer(DATA_OFF_DURATION);
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
        // Do nothing
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
        if (Prefs.getReconnectDuration(mContext) != 0) {
            mResidentService.changeState(new ResidentServiceScreenOffDataOff(
                    mContext, mResidentService));
        } else {
            mResidentService
                    .changeState(new ResidentServiceScreenOffDataOffEternal(
                            mContext, mResidentService));
        }
    }

    @Override
    public void onWifiConnected() {
        // Do nothing
    }
}
