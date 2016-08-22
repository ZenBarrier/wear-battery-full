package com.zenbarrier.wearfull;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
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

/**
 * Created by Anthony on 3/6/2015.
 */
public class WearListener extends WearableListenerService {
    private static final int UNPLUGGED_ID = 0x001;
    private static final int FULL_ID = 0x002;
    private static final int CHARGING_ID = 0x003;
    private static final int CONNECTION_LOST_ID = 0x004;

    public WearListener(){}


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        Log.d("WearListener",path);


        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //get app settings with manager
        final SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //show if charging?
        final boolean show_charging = mySharedPreferences.getBoolean("battery_charging",true);

        if(path.contains("/charging") && show_charging){
            int level =  Integer.parseInt(path.replaceAll("\\D", ""));
            chargingNotify(level);
            manager.cancel(FULL_ID);
            manager.cancel(UNPLUGGED_ID);
            return;
        }

        if(path.contains("/full")){
            manager.cancel(CHARGING_ID);
            manager.cancel(UNPLUGGED_ID);
            fullNotify();
        }
        if(path.contains("/unplugged")){
            manager.cancel(CHARGING_ID);
            unplugNotify();
        }

    }

    private void unplugNotify(){
        final Resources res = this.getResources();

        // This image is used as the notification's background on watch.
        final Bitmap picture = BitmapFactory.decodeResource(res, R.drawable.bad_background);

        //get app settings with manager
        final SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //is LED light on?
        final boolean light = mySharedPreferences.getBoolean("light",true);
        //what color LED light?
        final int color = Integer.parseInt(mySharedPreferences.getString("color", "-256"));
        //Should we vibrate?
        final boolean vibrate = mySharedPreferences.getBoolean("vibrate", true);
        //what sound?
        final String sound = mySharedPreferences.getString("ringtone_uri",
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString());
        final Uri alarmSound = Uri.parse(sound);



        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSound(alarmSound)
                .setSmallIcon(R.drawable.ic_power_lost)
                .setContentTitle("Unplugged")
                .setContentText("The watch was unplugged")
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        if(vibrate) {
            builder.setVibrate(new long[]{100, 100, 100, 100, 100});
        }
        if(light){
            builder.setLights(color, 500, 300);
        }

        //add background to watch by extending
        NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender();
        wearableExtender.setContentIcon(R.drawable.ic_power_lost);
        wearableExtender.setBackground(picture);

        //extend watch settings
        builder.extend(wearableExtender);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(UNPLUGGED_ID, builder.build());
    }

    private void fullNotify(){
        final Resources res = this.getResources();

        //This image is used as the notification's background on watch.
        final Bitmap picture = BitmapFactory.decodeResource(res, R.drawable.good_background);

        //get app settings with manager
        final SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //is LED light on?
        final boolean light = mySharedPreferences.getBoolean("light",true);
        //what color LED light?
        final int color = Integer.parseInt(mySharedPreferences.getString("color", "-256"));
        //Should we vibrate?
        final boolean vibrate = mySharedPreferences.getBoolean("vibrate", true);
        //Insistently alerting?
        final boolean insistent = mySharedPreferences.getBoolean("insistent", false);
        //what sound?
        final String sound = mySharedPreferences.getString("ringtone_uri",
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString());
        final Uri alarmSound = Uri.parse(sound);



        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSound(alarmSound)
                .setSmallIcon(R.drawable.ic_battery_full)
                .setContentTitle("Full")
                .setContentText("The watch is fully charged")
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        if(vibrate) {
            builder.setVibrate(new long[]{100, 100, 100, 100, 100});
        }
        if(light){
            builder.setLights(color, 500, 300);
        }

        //add background to watch by extending
        NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender();
        wearableExtender.setContentIcon(R.drawable.ic_battery_full);
        wearableExtender.setBackground(picture);

        //extend watch settings
        builder.extend(wearableExtender);

        Notification notification = builder.build();

        if(insistent) {
            notification.flags |= Notification.FLAG_INSISTENT;
        }

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(FULL_ID, notification);
    }

    private void chargingNotify(int level){

        //get app settings with manager
        final SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //show exact battery level?
        final boolean exact_battery_level = mySharedPreferences.getBoolean("exact_battery_level", false);



        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_battery_level)
                .setContentTitle("Watch Charging")
                .setContentText("Will alert when the watch is fully charged")
                .setAutoCancel(false)
                .setShowWhen(false)
                .setOngoing(true)
                        //do not show this on other devices
                .setLocalOnly(true)
                        //don't alert the phone every time this updates.
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW).setContentIntent(
                        PendingIntent.getActivity(
                                this,
                                0,
                                new Intent(this, MainActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT));

        if(exact_battery_level){
            //show progress level if true.
            builder.setProgress(100,level,false);
            builder.setContentText("Battery at "+level+"%");
            //this removes timestamp and stops flickering.
            builder.setWhen(0);
        }


        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(CHARGING_ID, builder.build());
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents){
       /* final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        ConnectionResult connectionResult = googleApiClient.blockingConnect(100,
                TimeUnit.MILLISECONDS);

        if (!connectionResult.isSuccess()) {
            Log.e("WearListener", "BatteryListenerService failed to connect to GoogleApiClient.");
            return;
        }else{
        }

        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = event.getDataItem();
                DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();
                boolean full = dataMap.getBoolean("full");
                if(full) {
                    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setVibrate(new long[]{100, 100, 100, 100, 100, 100, 100, 100})
                            .setColor(Color.YELLOW)
                            .setSound(uri)
                            .setContentText("full")
                            .setContentTitle("alert");
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.notify(1, builder.build());
                }
            }
            else if(event.getType() == DataEvent.TYPE_DELETED){

            }
        }*/
    }

    @Override
    public void onPeerDisconnected(com.google.android.gms.wearable.Node peer){

        //get app settings with manager
        final SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //alert disconnect?
        final boolean alert_disconnect = mySharedPreferences.getBoolean("disconnected", false);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        manager.cancel(CHARGING_ID);

        if(alert_disconnect) {

            final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_disconnected)
                    .setContentTitle("Watch disconnected")
                    .setContentText("Connection to the watch has been lost")
                    .setAutoCancel(true)
                            //do not show this on other devices
                    .setLocalOnly(true);

            manager.notify(CONNECTION_LOST_ID, builder.build());
        }
    }

    @Override
    public void onPeerConnected(com.google.android.gms.wearable.Node peer) {
        // Remove the "forgot phone" notification when connection is restored.
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .cancel(CONNECTION_LOST_ID);
    }
}
