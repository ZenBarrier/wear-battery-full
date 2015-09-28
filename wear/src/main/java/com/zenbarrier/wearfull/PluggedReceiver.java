package com.zenbarrier.wearfull;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Anthony on 3/2/2015.
 */
public class PluggedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent){

        Log.d(PluggedReceiver.class.getSimpleName(),"Plugged");

        //start power checker service
        Intent powerChecker = new Intent(ctx, PowerCheckerService.class);
        ctx.startService(powerChecker);

    }
}
