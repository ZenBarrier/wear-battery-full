package com.zenbarrier.wearfull;

import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationManager;
import android.support.wearable.complications.ComplicationProviderService;
import android.util.Log;

/**
 * Created by Anthony on 4/12/2017.
 * This file is the fragment that holds all the preferences
 */

public class MobileBatteryComplicationService extends ComplicationProviderService{

    private static final String TAG = MobileBatteryComplicationService.class.getSimpleName();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onComplicationUpdate(int complicationId, int dataType, ComplicationManager complicationManager) {
        Log.d(TAG, "Update: "+complicationId);

        ComplicationData.Builder complicationData = new ComplicationData.Builder(ComplicationData.TYPE_RANGED_VALUE)
                .setMinValue(0)
                .setMaxValue(100);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean isWatchConnected = preferences.getBoolean(getString(R.string.key_pref_connected), false);
        if(isWatchConnected){
            int level = preferences.getInt(getString(R.string.key_pref_mobile_battery_level), 0);
            complicationData.setValue(level)
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_phone_icon));
        }else{
            complicationData.setValue(0)
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_phone_disconnected));
        }


        complicationManager.updateComplicationData(complicationId, complicationData.build());
    }

    @Override
    public void onComplicationActivated(int complicationId, int type, ComplicationManager manager) {
        super.onComplicationActivated(complicationId, type, manager);
        Log.d(TAG, "activated: "+complicationId);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putBoolean(getString(R.string.key_pref_battery_complication_activated), true)
                .putInt(getString(R.string.key_pref_battery_complication_id), complicationId).apply();
    }

    @Override
    public void onComplicationDeactivated(int complicationId) {
        super.onComplicationDeactivated(complicationId);
        Log.d(TAG, "Deactivated: "+complicationId);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putBoolean(getString(R.string.key_pref_battery_complication_activated), false).apply();
    }
}
