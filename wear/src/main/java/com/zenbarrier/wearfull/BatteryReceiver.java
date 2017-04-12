package com.zenbarrier.wearfull;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.preference.PreferenceManager;

public class BatteryReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent){

        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int plug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE,-1);
        float batteryPct = 100 * level / (float)scale;

        //get app settings with manager
        final SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);

        //is notify if charging?
        final boolean charging = mySharedPreferences.getBoolean("battery_charging",true);
        //is notify level?
        final boolean exact_battery_level = mySharedPreferences.getBoolean("exact_battery_level",true);

        final int charge_level_alert = mySharedPreferences.getInt("charge_level_alert", 100);

        if(exact_battery_level && charging) {
            //send level to phone
            Intent notifyPhone = new Intent(ctx, NotifyMobileService.class);
            notifyPhone.putExtra("path", "/charging/" + level);
            notifyPhone.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startService(notifyPhone);
        }


        if(status == BatteryManager.BATTERY_STATUS_FULL || level == scale || batteryPct >= charge_level_alert) {

            //notify phone that wear device is full
            Intent notifyPhone = new Intent(ctx, NotifyMobileService.class);
            notifyPhone.putExtra("path","/full");
            notifyPhone.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startService(notifyPhone);

            //stop checking battery to save power
            ctx.unregisterReceiver(this);
        }
        if(plug==0){
            //Notify phone that Wear device was unplugged before finishing charging
            Intent notifyPhone = new Intent(ctx, NotifyMobileService.class);
            notifyPhone.putExtra("path","/unplugged");
            notifyPhone.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startService(notifyPhone);

            //stop checking battery to save power
            ctx.unregisterReceiver(this);
        }
    }
}
