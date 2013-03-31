package com.onedialogproject.galapagosmapho;

import com.onedialogproject.galapagosmapho.DebugTools.Pattern;
import com.onedialogproject.galapagosmapho.ResidentService.Carrier;

public class ResidentServiceScreenOffDataOff extends
        ResidentService.ResidentServiceState {

    public ResidentServiceScreenOffDataOff(ResidentService residentService) {
        super(residentService);
    }

    @Override
    public void start() {
        mResidentService.setOff();
        if (Prefs.getDebugMode(mContext)) {
            DebugTools.notify(mContext, Pattern.DATA_OFF);
        }
    }

    @Override
    public void end() {
        // Do nothing
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
