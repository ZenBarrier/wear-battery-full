package com.zenbarrier.wearfull;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Anthony on 3/2/2015.
 */
public class PowerCheckerService extends Service {


    private final BatteryReceiver receiver = new BatteryReceiver();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        //register the battery filter if plugged
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, filter);



        //get app settings with manager
        final SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //is notify if charging?
        final boolean charging = mySharedPreferences.getBoolean("battery_charging",true);

        if(charging) {
            //let the phone know that watch is charging
            Intent notifyPhone = new Intent(this, NotifyMobileService.class);
            notifyPhone.putExtra("path", "/charging/0");
            notifyPhone.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            this.startService(notifyPhone);
        }


        return START_NOT_STICKY;
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }


    @Override
    public void onDestroy(){
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
