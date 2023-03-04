package com.example.gefencingproject;

import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;

public class GeoFenceHelper extends ContextWrapper {
    private String Tag = "GeoFenceHelper";
    PendingIntent pendingIntent;
    public GeoFenceHelper(Context base) {
        super(base);
    }

    public GeofencingRequest getGeoFenceRequest(Geofence geofence){
        /**
         * .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
         * this will trigger if location is already in geo fence
         * */
        return new GeofencingRequest.Builder()
                .addGeofence(geofence)
                //.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER|GeofencingRequest.INITIAL_TRIGGER_EXIT)
                .build();
    }

    public Geofence getGeoFence(String geoFenceId, LatLng latLng, float radius, int transitionTypes){
        //.setLoiteringDelay(5000) is delay to notify Dwelling inside geoFence after 5000 mil sec

        return new Geofence.Builder()
                .setCircularRegion(latLng.latitude,latLng.longitude,radius)
                .setRequestId(geoFenceId)
                .setTransitionTypes(transitionTypes)
                .setLoiteringDelay(5000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    public PendingIntent getPendingIntent(){
        if(pendingIntent != null){
            return pendingIntent;
        }
        Intent intent = new Intent(this,GeoFenceBroadCastReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, Constants.GEO_FENCE_BROAD_CAST_RECEIVER_PENDING_INTENT_CODE,intent,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);
        return pendingIntent;
    }

    public String getError(Exception e){
        if(e instanceof ApiException){
            ApiException apiException = (ApiException)e;
            switch (apiException.getStatusCode()){
                case GeofenceStatusCodes
                        .GEOFENCE_NOT_AVAILABLE:
                    return "GEOFENCE_NOT_AVAILABLE";

                case GeofenceStatusCodes
                        .GEOFENCE_TOO_MANY_GEOFENCES:
                    return "GEOFENCE_TOO_MANY_GEOFENCES";

                case GeofenceStatusCodes
                        .GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    return "GEOFENCE_TOO_MANY_PENDING_INTENTS";
            }
        }
            return e.getLocalizedMessage();

    }

}
