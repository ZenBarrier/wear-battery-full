package com.zenbarrier.wearfull;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationManager;
import android.support.wearable.complications.ComplicationProviderService;
import android.support.wearable.complications.ComplicationText;
import android.support.wearable.complications.ProviderUpdateRequester;
import android.util.Log;

import com.google.android.wearable.intent.RemoteIntent;

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

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(getString(R.string.key_pref_battery_complication_id), complicationId);

        boolean hasResult = preferences.getBoolean(getString(R.string.key_pref_after_mobile_result), false);
        boolean isWatchConnected = preferences.getBoolean(getString(R.string.key_pref_connected), false);
        int level = preferences.getInt(getString(R.string.key_pref_mobile_battery_level), 0);

        PendingIntent pendingIntent = PendingIntent.getService(
                this, complicationId, new Intent(this, UpdateComplicationActionService.class), 0);

        ComplicationData.Builder complicationData = new ComplicationData.Builder(ComplicationData.TYPE_RANGED_VALUE)
                .setMinValue(0)
                .setTapAction(pendingIntent)
                .setMaxValue(100);

        if (isWatchConnected) {
            complicationData.setValue(level)
                    .setShortText(ComplicationText.plainText(level+"%"))
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_phone_icon));
        } else {
            complicationData.setValue(0)
                    .setShortText(ComplicationText.plainText("Severed"))
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_phone_disconnected));
        }


        complicationManager.updateComplicationData(complicationId, complicationData.build());

        if(!hasResult){
            NotifyMobileService.sendMessage(this, "/request_battery");
        }
        else{
            editor.putBoolean(getString(R.string.key_pref_after_mobile_result), false);
        }
        editor.apply();
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

    public static void updateBatteryComplication(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Log.d(TAG, "update");

        boolean isActiveComplication =
                preferences.getBoolean(context.getString(R.string.key_pref_battery_complication_activated), false);
        if(isActiveComplication) {
            int complicationId =
                    preferences.getInt(context.getString(R.string.key_pref_battery_complication_id), -1);

            ComponentName componentName =
                    new ComponentName(context, MobileBatteryComplicationService.class);

            ProviderUpdateRequester providerUpdateRequester =
                    new ProviderUpdateRequester(context, componentName);

            providerUpdateRequester.requestUpdate(complicationId);
        }
    }

    public static class UpdateComplicationActionService extends IntentService{

        public UpdateComplicationActionService() {
            super("UpdateComplicationActionService");
        }

        @Override
        protected void onHandleIntent(@Nullable Intent intent) {
            Log.d(UpdateComplicationActionService.class.getSimpleName(), "Intent called");
            updateBatteryComplication(this);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean isConnected = preferences.getBoolean(getString(R.string.key_pref_connected), true);

            if(!isConnected) {
                Intent intentAndroid =
                        new Intent(Intent.ACTION_VIEW)
                                .addCategory(Intent.CATEGORY_BROWSABLE)
                                .setData(Uri.parse(MainActivity.PLAY_STORE_APP_URI));

                RemoteIntent.startRemoteActivity(
                        this,
                        intentAndroid,
                        null);
            }
        }
    }
}
