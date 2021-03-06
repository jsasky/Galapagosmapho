package com.onedialogproject.galapagosmapho;

import com.onedialogproject.galapagosmapho.DebugTools.Pattern;
import com.onedialogproject.galapagosmapho.ResidentService.Carrier;
import com.onedialogproject.galapagosmapho.ResidentService.ServiceState;

public class ResidentServiceScreenOffCharging extends
        ResidentService.ResidentServiceState {

    public ResidentServiceScreenOffCharging(ResidentService residentService) {
        super(residentService);
    }

    @Override
    public void start() {
        Log.append(mContext, "充電状態のためネット接続中");
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
    public ServiceState getState() {
        return ServiceState.SCREEN_OFF_CHARGING;
    }

    @Override
    public void onScreenOn() {
        mResidentService.changeState(new ResidentServiceScreenOn(
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
        mResidentService.changeState(new ResidentServiceScreenOffDataOn(
                mResidentService));
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
