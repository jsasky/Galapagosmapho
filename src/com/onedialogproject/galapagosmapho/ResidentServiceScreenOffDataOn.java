package com.onedialogproject.galapagosmapho;

import com.onedialogproject.galapagosmapho.DebugTools.Pattern;
import com.onedialogproject.galapagosmapho.ResidentService.Carrier;
import com.onedialogproject.galapagosmapho.ResidentService.ServiceState;

public class ResidentServiceScreenOffDataOn extends
        ResidentService.ResidentServiceState {

    private static final int DATA_OFF_DURATION = 10;// sec

    public ResidentServiceScreenOffDataOn(ResidentService residentService) {
        super(residentService);
    }

    @Override
    public void start() {
        mResidentService.setOn();
        if (Prefs.getDebugMode(mContext)) {
            DebugTools.notify(mContext, Pattern.DATA_ON);
        }
        Log.append(mContext, "切断タイマー開始:切断まで" + DATA_OFF_DURATION + "秒");
        mResidentService.startTimer(DATA_OFF_DURATION);
    }

    @Override
    public void end() {
        mResidentService.cancelTimer();
    }

    @Override
    public ServiceState getState() {
        return ServiceState.SCREEN_OFF_DATA_ON;
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
        // Do nothing
    }

    @Override
    public void onNotifyReceiveMail(Carrier carrier) {
        notifyReceiveMail(carrier);
    }

    @Override
    public void onTimerExpired() {
        mResidentService.changeState(new ResidentServiceScreenOffDataOff(
                mResidentService));
    }

    @Override
    public void onWifiConnected() {
        // Do nothing
    }
}
