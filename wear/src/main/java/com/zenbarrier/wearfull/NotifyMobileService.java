

package com.zenbarrier.wearfull;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;


public class NotifyMobileService extends IntentService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final long TIME_OUT_MS = 100;
    private static final String TAG = NotifyMobileService.class.getSimpleName();


    public NotifyMobileService() {
        super(NotifyMobileService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    private void sendMessageToWearable(final String path) {
        //connect to the google API
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        //Get the connection result
        ConnectionResult result = googleApiClient.blockingConnect(TIME_OUT_MS,
                TimeUnit.MILLISECONDS);

        //Check if connected
        if (!result.isSuccess()) {
            Log.e(TAG, "Failed to connect to GoogleApiClient.");
            return;
        }

        //Get nodes
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(googleApiClient).await();

        //send message through all nodes
        for (Node node : nodes.getNodes()) {
            Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), path,
                    new byte[0]);
            Log.d(TAG,"sending");
        }
        googleApiClient.disconnect();

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //get the intent path for the message
        String path = intent.getStringExtra("path");
        Log.d(TAG,"path:"+path);
        //send message with the path
        sendMessageToWearable(path);
    }



    @Override
    public void onConnected(Bundle connectionHint) {
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.e(TAG,"failed to connect");
    }
}
