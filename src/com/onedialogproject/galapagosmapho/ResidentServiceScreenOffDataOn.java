package com.onedialogproject.galapagosmapho;

import com.onedialogproject.galapagosmapho.DebugTools.Pattern;

import android.content.Context;

public class ResidentServiceScreenOffDataOn extends
        ResidentService.ResidentServiceState {

    private static final int DURATION = 60;// sec

    public ResidentServiceScreenOffDataOn(Context context,
            ResidentService residentService) {
        super(context, residentService);
    }

    @Override
    public void start() {
        mResidentService.setOn();
        if (Prefs.getDebugMode(mContext)) {
            DebugTools.notify(mContext, Pattern.DATA_ON);
        }
        Utils.addLog(mContext, "タイマー開始:切断まで" + DURATION + "秒");
        mResidentService.startTimer(DURATION);
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
    public void onNotifyReceiveMail() {
        Utils.addLog(mContext, "メール着信通知");
        Utils.notify(mContext);
    }

    @Override
    public void onTimerExpired() {
        mResidentService.changeState(new ResidentServiceScreenOffDataOff(
                mContext, mResidentService));
    }
}
