package com.zenbarrier.wearfull;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;



public class MainActivity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addOnConnectionFailedListener(this)
                .build();

        syncPreferencesToWearable(this);
        new StartWearableActivityTask().execute();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        new StartWearableActivityTask().execute();

    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startPreferencesActivity(null);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("Wear", "Failed to connect to Google Play Services");
        Toast.makeText(this,"Failed to connect to wear.",Toast.LENGTH_LONG).show();
    }

    private void sendStartActivityMessage() {

        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for(Node node : nodes.getNodes()) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, node.getId(), "/start", new byte[0]);
            Log.d(TAG,"sending");
        }
    }

    public void writeReview(View view) {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent marketIntent = new Intent(Intent.ACTION_VIEW, uri);
        try{
            startActivity(marketIntent);
        }catch (ActivityNotFoundException e){
            Toast.makeText(this, " unable to find market app", Toast.LENGTH_LONG).show();
        }
    }

    private class StartWearableActivityTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
            sendStartActivityMessage();
            return null;
        }
    }

    /**
     * Sends an RPC to start a fullscreen Activity on the wearable.
     */
    public void startPreferencesActivity(View view) {
        Log.d(TAG, "Generating RPC");

        Intent intent = new Intent();
        intent.setClass(MainActivity.this, SetPreferenceActivity.class);
        startActivityForResult(intent, 0);

    }

    public void syncPreferencesToWearable(Context context) {
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
        Wearable.DataApi.putDataItem(mGoogleApiClient, putRequest.asPutDataRequest());


    }


}


