package com.onedialogproject.galapagosmapho;

import android.content.Context;

import com.onedialogproject.galapagosmapho.DebugTools.Pattern;
import com.onedialogproject.galapagosmapho.ResidentService.Carrier;

public class ResidentServiceScreenOffDataOffEternal extends
        ResidentService.ResidentServiceState {

    public ResidentServiceScreenOffDataOffEternal(Context context,
            ResidentService residentService) {
        super(context, residentService);
    }

    @Override
    public void start() {
        mResidentService.setOff();
        if (Prefs.getDebugMode(mContext)) {
            DebugTools.notify(mContext, Pattern.DATA_OFF);
        }
        Log.append(mContext, "接続タイマー作動なし:自動再接続はしません");
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
    public void onNotifyReceiveMail(Carrier carrier) {
        notifyReceiveMail(carrier);
    }

    @Override
    public void onTimerExpired() {
        // Do nothing
    }

    @Override
    public void onWifiConnected() {
        // Do nothing
    }
}
