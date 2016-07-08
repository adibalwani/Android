package edu.neu.madcourse.adibalwani.twoplayerwordgame;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class GCMBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName component =
                new ComponentName(context.getPackageName(), GCMIntentService.class.getName());
        startWakefulService(context, intent.setComponent(component));
        setResultCode(Activity.RESULT_OK);
    }
}
