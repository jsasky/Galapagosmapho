package com.onedialogproject.galapagosmapho;

import com.onedialogproject.galapagosmapho.DebugTools.Pattern;

import android.content.Context;

public class ResidentServiceScreenOffCharging extends
        ResidentService.ResidentServiceState {

    public ResidentServiceScreenOffCharging(Context context,
            ResidentService residentService) {
        super(context, residentService);
    }

    @Override
    public void start() {
        Utils.addLog(mContext, "充電状態のためネット接続中");
        mResidentService.setOn();
        if (Prefs.getDebugMode(mContext)) {
            DebugTools.notify(mContext, Pattern.CHARGING);
        }
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
        // Do nothing
    }

    @Override
    public void onNotCharging() {
        if (Prefs.getDelayOff(mContext)) {
            mResidentService.changeState(new ResidentServiceScreenOffDataOn(
                    mContext, mResidentService));
        } else {
            mResidentService
                    .changeState(new ResidentServiceScreenOffNotCharging(
                            mContext, mResidentService));
        }
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
