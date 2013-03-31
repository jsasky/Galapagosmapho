package com.onedialogproject.galapagosmapho;

import com.onedialogproject.galapagosmapho.R;
import com.onedialogproject.galapagosmapho.DebugTools.Pattern;
import com.onedialogproject.galapagosmapho.ResidentService.ChargingState;

import android.content.Context;

public class ResidentServiceScreenOn extends
        ResidentService.ResidentServiceState {

    public ResidentServiceScreenOn(Context context,
            ResidentService residentService) {
        super(context, residentService);
    }

    @Override
    public void start() {
        if (Prefs.getDebugMode(mContext)) {
            DebugTools.notify(mContext, Pattern.SCREEN_ON);
        }

        if (mResidentService.setOn()) {
            NotifyAreaController.putNotice(mContext, R.string.notice_lcd_on,
                    R.string.notify_explanation);
        }
    }

    @Override
    public void end() {
        // Do nothing
    }

    @Override
    public void onScreenOn() {
        // Do nothing
    }

    @Override
    public void onScreenOff() {
        if (Prefs.getDelayOff(mContext)) {
            if (mResidentService.getChargingState() == ChargingState.CHARGING) {
                mResidentService
                        .changeState(new ResidentServiceScreenOffCharging(
                                mContext, mResidentService));
            } else {
                mResidentService
                        .changeState(new ResidentServiceScreenOffDataOn(
                                mContext, mResidentService));
            }
        } else {
            mResidentService
                    .changeState(new ResidentServiceScreenOffNotCharging(
                            mContext, mResidentService));
        }
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
        // Do nothing
    }

    @Override
    public void onTimerExpired() {
        // Do nothing
    }

}
