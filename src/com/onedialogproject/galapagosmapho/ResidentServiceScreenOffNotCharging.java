package com.onedialogproject.galapagosmapho;

import android.content.Context;

public class ResidentServiceScreenOffNotCharging extends
        ResidentService.ResidentServiceState {

    public ResidentServiceScreenOffNotCharging(Context context,
            ResidentService residentService) {
        super(context, residentService);
    }

    @Override
    public void start() {
        mResidentService.setOff();
    }

    @Override
    public void end() {
        // Do nothing
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
        // Do nothing
    }
}
