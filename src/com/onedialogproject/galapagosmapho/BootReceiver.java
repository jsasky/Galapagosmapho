package com.onedialogproject.galapagosmapho;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ((context == null) || (intent == null)) {
            return;
        }

        String action = intent.getAction();
        if ((action != null) && Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Log.append(context, "端末再起動");
            if (Prefs.isActivated(context)
                    && !ResidentService.isServiceRunning(context)) {
                Prefs.setActiveFlag(context, false);
                context.startService(new Intent(context, ResidentService.class));
            }
        }
    }
}
