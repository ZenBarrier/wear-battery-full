package com.zenbarrier.wearfull;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

public class NotifyMobileService extends IntentService {

    private static final String EXTRA_PARAM = "path";
    private static final long TIME_OUT_MS = 100;
    private static final String TAG = NotifyMobileService.class.getSimpleName();

    public NotifyMobileService() {
        super("NotifyMobileService");
    }

    public static void sendMessage(Context context, String path) {
        Intent intent = new Intent(context, NotifyMobileService.class);
        intent.putExtra(EXTRA_PARAM, path);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String param = intent.getStringExtra(EXTRA_PARAM);
            sendMessageToWearable(param);
        }
    }

    private void sendMessageToWearable(final String path) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        ConnectionResult result = googleApiClient.blockingConnect(TIME_OUT_MS,
                TimeUnit.MILLISECONDS);

        if (!result.isSuccess()) {
            Log.e(TAG, "Failed to connect to GoogleApiClient.");
            return;
        }

        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(googleApiClient).await();

        for (Node node : nodes.getNodes()) {
            Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), path,
                    new byte[0]);
            Log.d(TAG,"sending");
        }
        googleApiClient.disconnect();

    }

}
