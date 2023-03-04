package com.example.gefencingproject;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;

import android.os.IBinder;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;



import com.google.android.gms.maps.model.LatLng;

public class LocationService extends Service implements LocationListener {

    MutableLiveData<LatLng> mutableLiveData = new MutableLiveData<>();
    public final long UPDATE_INTERVAL = 1000;
    public static final int NOTIFICATION_ID = 200;
    public static final int NOTIFICATION_ID_FOREGROUND = 201;
    boolean firstLocationByNetwork = true;
    @Override
    public void onCreate() {
        super.onCreate();
        startLocationUpdates();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(NOTIFICATION_ID, new Notification());
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        // Do your Stuff
        startLocationUpdates();
        return START_STICKY;

    }

    @Override
    public void onLocationChanged(Location location) {
        //Toast.makeText(getApplicationContext(),"onLocationChanged", Toast.LENGTH_SHORT).show();
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mutableLiveData.setValue(latLng);
    }

    public void startLocationUpdates() {

        LocationManager mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (mLocationManager != null) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_INTERVAL, 0, this);
            }
        }


    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(NOTIFICATION_ID_FOREGROUND, notification);
    }


    private final IBinder myBinder = new MyLocalBinder();

    @Override
    public IBinder onBind(Intent arg0) {
        return myBinder;
    }

    public class MyLocalBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }
}
