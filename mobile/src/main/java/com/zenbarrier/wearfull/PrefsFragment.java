package com.zenbarrier.wearfull;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by Anthony on 3/18/2015.
 * This file is the fragment that holds all the preferences
 */
public class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int CHARGING_ID = 0x003;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //get shared prefs
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        //get context
        Context ctx = this.getActivity();

        //load sound title on start
        try {
            Uri uri = Uri.parse(sharedPreferences.getString(getString(R.string.key_pref_ringtone_uri), ""));
            String title = RingtoneManager.getRingtone(ctx, uri).getTitle(ctx);
            Log.d(PrefsFragment.class.getSimpleName(), title);
            findPreference(getString(R.string.key_pref_ringtone_uri)).setSummary(title);
        }
        catch(Exception e){
            findPreference(getString(R.string.key_pref_ringtone_uri)).setSummary("");
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(isAdded()) {
            Context ctx = this.getActivity();
            if (key.equals(getString(R.string.key_pref_ringtone_uri))) {
                Preference preference = findPreference(key);
                Uri uri = Uri.parse(sharedPreferences.getString(key, ""));
                String title = RingtoneManager.getRingtone(ctx, uri).getTitle(ctx);
                Log.d(PrefsFragment.class.getSimpleName(), title);
                preference.setSummary(title);
                if(title.toLowerCase().contains("unknown")){
                    preference.setSummary("Silent");
                }

            }
            syncPreferencesToWearable(ctx);

            if(key.contains(getString(R.string.key_pref_battery_charging))) {
                //cancel the charging notification if set off
                if(!sharedPreferences.getBoolean(key,true)) {
                    ((NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE))
                            .cancel(CHARGING_ID);
                }
            }
        }
    }

    public void syncPreferencesToWearable(Context context) {
        GoogleApiClient mGoogleApiClient;
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult result) {
                    }
                })
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        //if(mGoogleApiClient==null)
         //   return;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean charging = sharedPreferences.getBoolean(getString(R.string.key_pref_battery_charging), true);
        Boolean level = sharedPreferences.getBoolean(getString(R.string.key_pref_exact_battery_level),false);
        int alert_level = Integer.parseInt(sharedPreferences.getString(getString(R.string.key_pref_charge_level_alert), "100"));

        final PutDataMapRequest putRequest = PutDataMapRequest.create("/PREF");

        final DataMap map = putRequest.getDataMap();
        map.putBoolean(getString(R.string.key_pref_battery_charging),charging);
        map.putBoolean(getString(R.string.key_pref_exact_battery_level), level);
        map.putInt(getString(R.string.key_pref_charge_level_alert), alert_level);
        Wearable.DataApi.putDataItem(mGoogleApiClient,  putRequest.asPutDataRequest());


    }
}
