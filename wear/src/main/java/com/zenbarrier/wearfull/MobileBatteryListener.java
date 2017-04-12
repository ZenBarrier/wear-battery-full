package com.zenbarrier.wearfull;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MobileBatteryListener extends WearableListenerService {
    private static final long TIMEOUT_MS = 100;
    private static final String TAG = MobileBatteryListener.class.getSimpleName();

    public MobileBatteryListener() {
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents){
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.release();

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        ConnectionResult connectionResult = googleApiClient.blockingConnect(TIMEOUT_MS,
                TimeUnit.MILLISECONDS);

        //powerServ = new Intent(this, PowerChecker.class);
        if (!connectionResult.isSuccess()) {
            Log.e("BatteryListener", "BatteryListenerService failed to connect to GoogleApiClient.");
            return;
        }

        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = event.getDataItem();
                Uri path = dataItem.getUri();
                DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();
                //get preferences from the mobile app
                if(path.toString().contains("/PREF")){
                    //charging pref bool
                    Boolean battery_charging = dataMap.getBoolean("battery_charging");
                    //show level pref bool
                    Boolean exact_battery_level = dataMap.getBoolean("exact_battery_level");

                    int charge_level_alert = dataMap.getInt("charge_level_alert", 100);

                    //get the pref manager
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                    //open an editor to store prefs
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("battery_charging",battery_charging);
                    editor.putBoolean("exact_battery_level",exact_battery_level);
                    editor.putInt("charge_level_alert", charge_level_alert);
                    //store the prefs
                    editor.apply();

                    //if show charging is true, resend the signal
                    if(battery_charging){
                        //get battery intent
                        Intent bat = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                        //check if charging
                        int status = bat != null ? bat.getIntExtra(BatteryManager.EXTRA_STATUS, -1) : -1;
                        int level = bat != null ? bat.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
                        //check if plugged. -1 is unplugged
                        int plug = bat != null ? bat.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) : -1;

                        //check if charging and plugged in
                        if(status == BatteryManager.BATTERY_STATUS_CHARGING && plug!=-1){
                            //let the phone know that watch is charging
                            Intent notifyPhone = new Intent(this, NotifyMobileService.class);
                            notifyPhone.putExtra("path", "/charging/"+level);
                            notifyPhone.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            this.startService(notifyPhone);

                            //start power checker service to register receiver
                            Intent powerChecker = new Intent(this, PowerCheckerService.class);
                            this.startService(powerChecker);
                        }
                    }

                }
            }
        }

    }
    @Override
    public void onMessageReceived(MessageEvent messageEvent){
        //Toast.makeText(this,"started",Toast.LENGTH_LONG).show();
        Log.d(TAG,"got message to start!");
    }

    @Override
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        super.onCapabilityChanged(capabilityInfo);
        if(capabilityInfo.getNodes().size() > 0){
            reconnectedWatch();
        }
    }


    private void reconnectedWatch() {
        //get battery intent
        Intent bat = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        //check if charging
        int status = bat != null ? bat.getIntExtra(BatteryManager.EXTRA_STATUS, -1) : -1;
        int level = bat != null ? bat.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        //check if plugged. 0 is unplugged
        int plug = bat != null ? bat.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) : -1;

        //check if charging and plugged in
        if(status == BatteryManager.BATTERY_STATUS_CHARGING && plug>0){
            //let the phone know that watch is charging
            Intent notifyPhone = new Intent(this, NotifyMobileService.class);
            notifyPhone.putExtra("path", "/charging/"+level);
            notifyPhone.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            this.startService(notifyPhone);

            //start power checker service to register receiver
            Intent powerChecker = new Intent(this, PowerCheckerService.class);
            this.startService(powerChecker);
        }
    }


}
