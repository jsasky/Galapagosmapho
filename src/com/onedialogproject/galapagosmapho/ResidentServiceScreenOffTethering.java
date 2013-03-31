package com.onedialogproject.galapagosmapho;

import com.onedialogproject.galapagosmapho.DebugTools.Pattern;
import com.onedialogproject.galapagosmapho.ResidentService.Carrier;
import com.onedialogproject.galapagosmapho.ResidentService.ServiceState;

public class ResidentServiceScreenOffTethering extends
        ResidentService.ResidentServiceState {

    public ResidentServiceScreenOffTethering(ResidentService residentService) {
        super(residentService);
    }

    @Override
    public void start() {
        Log.append(mContext, "テザリング中のためネット接続中");
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
        return ServiceState.SCREEN_OFF_TETHERING;
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
        mResidentService.changeState(new ResidentServiceScreenOffCharging(
                mResidentService));
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
