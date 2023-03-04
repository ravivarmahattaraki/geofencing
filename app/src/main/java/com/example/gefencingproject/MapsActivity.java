package com.example.gefencingproject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;


import android.Manifest;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private Boolean autoRecenter = false;
    private static final float RADIUS = 100.0f;
    private GoogleMap mMap;
    private final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION};

    private GeofencingClient geofencingClient;
    private GeoFenceHelper geoFenceHelper;
    private final String TAG = "MapsActivity";

    private final ArrayList<String> list = new ArrayList<>();


    LocationService service;
    boolean isBound = false;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            Log.d(TAG, "ServiceConnection: connected to service.");
            LocationService.MyLocalBinder binder = (LocationService.MyLocalBinder) iBinder;
            service = binder.getService();
            service.mutableLiveData.observe(MapsActivity.this, latLng -> {
                if(mMap == null){
                    return;
                }
                if(!autoRecenter){
                    return;
                }
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
                mMap.animateCamera(cameraUpdate);
            });
            isBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "ServiceConnection: disconnected from service.");
            isBound = false;
        }
    };
    private void startService(){
        Intent serviceIntent = new Intent(this, LocationService.class);
        startService(serviceIntent);

        bindService();
    }

    private void bindService(){
        Intent serviceBindIntent =  new Intent(this, LocationService.class);
        bindService(serviceBindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        geofencingClient = LocationServices.getGeofencingClient(this);
        geoFenceHelper = new GeoFenceHelper(this);

        Button button = findViewById(R.id.remove_geofence);
        Button autoRecenterBtn = findViewById(R.id.auto_recenter);
        button.setOnClickListener(view -> removeGeofences());
        autoRecenterBtn.setOnClickListener(view -> {
            autoRecenter = !autoRecenter;
        });

        startService();


    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();
        LatLng latLng = new LatLng(16.078559, 74.659612);
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("karaguppi");
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
        mMap.addMarker(markerOptions);
        mMap.animateCamera(cameraUpdate);
        enableLocation();

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMapLongClickListener(this);
    }

    private void enableBackGroundLocation(LatLng latLng) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            handleMapLongClick(latLng);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{permissions[1]}, Constants.BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permissions[1]}, Constants.BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
            }
        }
    }

    private void enableLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //show why permission is need and ask for permission
                ActivityCompat.requestPermissions(this, new String[]{permissions[0]}, Constants.FINE_LOCATION_ACCESS_REQUEST_CODE);
            } else {
                //ask for permission
                ActivityCompat.requestPermissions(this, new String[]{permissions[0]}, Constants.FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission is granted
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
                service.startLocationUpdates();
            }
        }
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        enableBackGroundLocation(latLng);
    }

    private void handleMapLongClick(LatLng latLng) {
        //mMap.clear();
        addMarker(latLng);
        addCircle(latLng, RADIUS);
        addGeoFence(latLng, RADIUS);
    }

    public void addMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        mMap.addMarker(markerOptions);
    }

    public void addCircle(LatLng latLng, float radius) {

        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);
    }

    int count = 0;

    public void addGeoFence(LatLng latLng, float radius) {
        count++;
        Geofence geofence = geoFenceHelper.getGeoFence("my_geofence" + count, latLng, radius,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geoFenceHelper.getGeoFenceRequest(geofence);
        PendingIntent pendingIntent = geoFenceHelper.getPendingIntent();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        Log.e(TAG + "COUNT", "count: " + count);

        //Toast.makeText(getApplicationContext(),geofencingRequest.getGeofences().size()+"",Toast.LENGTH_SHORT).show();

        geofencingClient.addGeofences(geofencingRequest, pendingIntent).addOnSuccessListener(aVoid -> {
            list.add(geofence.getRequestId());
            Log.e(TAG, "onSuccess() : Geo Fence Added");
            Toast.makeText(getApplicationContext(), "Geo Fence Added", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            String errorMessage = geoFenceHelper.getError(e);
            Log.e(TAG, "onFailuer() : " + errorMessage);
            e.printStackTrace();
        });

    }

    public void removeGeofences() {
        if (geofencingClient != null && mMap != null) {
            mMap.clear();
            Toast.makeText(getApplicationContext(), "Removed", Toast.LENGTH_SHORT).show();
            geofencingClient.removeGeofences(list);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        removeGeofences();
    }
}