package com.example.gefencingproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;

public class GeoFenceBroadCastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeoFenceBroadCast";
    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //Toast.makeText(context, "geo fence triggered",Toast.LENGTH_SHORT).show();

        this.context = context;
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if(geofencingEvent == null){
            return;
        }
        if(geofencingEvent.hasError()){
            Log.e(TAG,"Error : while receiving geo fencing event");
            return;
        }
        if(geofencingEvent.getTriggeringLocation() == null){
            return;
        }
        int transistionType = geofencingEvent.getGeofenceTransition();
        switch (transistionType){

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                showMessgae(context, geofencingEvent, "ENTER" +
                        "\nlat: " + geofencingEvent.getTriggeringLocation().getLatitude() +
                        "\nlon: " + geofencingEvent.getTriggeringLocation().getLongitude(), 101);
                break;

            case Geofence.GEOFENCE_TRANSITION_DWELL:
                showMessgae(context, geofencingEvent, "DWELL", 102);
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                showMessgae(context, geofencingEvent, "EXIT" +
                        "\nlat: " + geofencingEvent.getTriggeringLocation().getLatitude() +
                        "\nlon: " + geofencingEvent.getTriggeringLocation().getLongitude(), 103);
                break;
        }
    }

    private void showMessgae(Context context, GeofencingEvent geofencingEvent, String geofencingEvent1, int id) {
        Toast.makeText(context,  geofencingEvent1+
                "\nlat: "+ geofencingEvent.getTriggeringLocation().getLatitude()+
                "\nlon: "+ geofencingEvent.getTriggeringLocation().getLongitude(),
                Toast.LENGTH_LONG).show();

        sendNotification("Ravivarma geo app", geofencingEvent1, id);
    }

    public void sendNotification(String title, String content, int id) {

        //Get an instance of NotificationManager//

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context,id+"")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(title)
                        .setContentText(content);


        // Gets an instance of the NotificationManager service//

        NotificationManager mNotificationManager =

                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "Your_channel_id";
        NotificationChannel channel = new NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH);
        mNotificationManager.createNotificationChannel(channel);
        mBuilder.setChannelId(channelId);

        mNotificationManager.notify(id, mBuilder.build());
    }
}
