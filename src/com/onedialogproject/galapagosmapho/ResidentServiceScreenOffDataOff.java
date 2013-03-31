package com.onedialogproject.galapagosmapho;

import com.onedialogproject.galapagosmapho.DebugTools.Pattern;

import android.content.Context;

public class ResidentServiceScreenOffDataOff extends
        ResidentService.ResidentServiceState {

    private static final int DURATION = 60 * 60;// sec

    public ResidentServiceScreenOffDataOff(Context context,
            ResidentService residentService) {
        super(context, residentService);
    }

    @Override
    public void start() {
        mResidentService.setOff();
        int duration = DURATION;
        if (Prefs.getDebugMode(mContext)) {
            DebugTools.notify(mContext, Pattern.DATA_OFF);
            duration = 60;// sec
        }
        Utils.addLog(mContext, "タイマー開始:接続まで" + duration + "秒");
        mResidentService.startTimer(duration);
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
    public void onNotifyReceiveMail() {
        Utils.addLog(mContext, "メール着信通知");
        Utils.notify(mContext);
    }

    @Override
    public void onTimerExpired() {
        mResidentService.changeState(new ResidentServiceScreenOffDataOn(
                mContext, mResidentService));
    }
}
