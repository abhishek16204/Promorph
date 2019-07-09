package com.example.android.promorph;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationService extends Service implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, LocationListener {

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";


    public static final String TAG = LocationService.class.getSimpleName();

    GoogleApiClient mLocationClient;
    LocationRequest mLocationRequest = new LocationRequest();

    FusedLocationProviderClient fusedLocationProviderClient;
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    public static final String EXTRA_ACCURACY = "extra_accuracy";
    public static final String EXTRA_DISTANCE = "extra_distance";
    public static final String ACTION_LOCATION_BROADCAST = LocationService.class.getName() + "LocationBroadcast";
    double latitude;
    double longitude;
    double accuracy;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

       /* if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Hello")
                    .setAutoCancel(true)
                    .setContentText("World").build();

            startForeground(0, notification);
        }*/


        Log.e(TAG, "inside onStart");

        mLocationClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();


       /* mLocationRequest.setInterval(10 * 1000);

        mLocationRequest.setFastestInterval(5 * 1000);*/

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //mLocationRequest.setSmallestDisplacement(50);
        fusedLocationProviderClient = new FusedLocationProviderClient(getApplicationContext());
        TempActivity.status = intent.getStringExtra("status");
        TempActivity.flag = intent.getIntExtra("flag", 1);
        String stop = intent.getStringExtra("stop");
        if (stop != null) {
            Toast.makeText(getApplicationContext(), "Service stopped", Toast.LENGTH_SHORT).show();
            stopSelf();

        }

        Toast.makeText(getApplicationContext(), "Value sent from attendance " + TempActivity.flag + "     " + TempActivity.status, Toast.LENGTH_SHORT).show();

        Log.e("Value of flag  ", TempActivity.flag + " ");
        Log.e("Value of status  ", TempActivity.status + " ");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Please provide required permissions to the app first ", Toast.LENGTH_LONG).show();

            return START_STICKY;
        } else {
            fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);

                    latitude = locationResult.getLastLocation().getLatitude();

                    longitude = locationResult.getLastLocation().getLongitude();

                    accuracy = locationResult.getLastLocation().getAccuracy();


                    double distance = distance(26.5156114, 80.2304174, latitude, longitude, accuracy);
                    if (distance != -1)
                        sendMessageToUI(String.valueOf(latitude), String.valueOf(longitude), String.valueOf(accuracy), String.valueOf(distance), TempActivity.flag);

                    //taking only 3 digits after decimal of longitude and latitude
                    Log.e("xyz", "Latitude: " + latitude + " Longitude: " + longitude + " accuracy: " + accuracy + " flag: " + TempActivity.flag + "distance " + distance);
                }

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability) {
                    super.onLocationAvailability(locationAvailability);
                }
            }, getMainLooper());


            mLocationClient.connect();

            return START_STICKY;
            //return super.onStartCommand(intent, flags, startId);
        }


    }

    @Override
    public void onDestroy() {
        Intent broadcastIntent = new Intent(this, Restarter.class);
        broadcastIntent.setAction("restart service");
        broadcastIntent.setClass(this, Restarter.class);
        Toast.makeText(getApplicationContext(), "Value sent from destroy " + TempActivity.flag + "     " + TempActivity.status, Toast.LENGTH_SHORT).show();
        Log.e("flag sent from intent", "  " + TempActivity.flag);
        //sendBroadcast(broadcastIntent);
        super.onDestroy();

    }

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Failed to connect to Google API");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location changed");


        if (location != null) {
            Log.d(TAG, "== location != null");

            Log.e(TAG, location.getLatitude() + " lng  " + location.getLongitude() + " accuracy  " + location.getAccuracy());
            double distance = distance(26.5156114, 80.2304174, latitude, longitude, accuracy);
            //Send result to activities
            if (distance != -1)
                sendMessageToUI(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), String.valueOf(location.getAccuracy()), String.valueOf(distance), TempActivity.flag);
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void sendMessageToUI(String lat, String lng, String accuracy, String distance, int flag) {

        Log.e("lat="+lat+"   lon="+lng,"   acc="+accuracy+"    dis="+distance+"   flag="+flag);
        if (Double.parseDouble(distance) < 10 && TempActivity.flag == 0 && TempActivity.status.equals("CHECK-IN")) {
            TempActivity.flag = 1;
            Log.e("flag updated to ", "  " + TempActivity.flag);
            Toast.makeText(getApplicationContext(), "Notify me!!", Toast.LENGTH_SHORT).show();
            Intent resultIntent = new Intent(getApplicationContext(), AttendanceActivity.class);
            resultIntent.putExtra("flag", TempActivity.flag);
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(),
                    0 /* Request code */, resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            String msg = "YOU ARE INSIDE OFFICE PREMISES. YOU NEED TO  " + TempActivity.status;

            mBuilder = new NotificationCompat.Builder(getApplicationContext());
            mBuilder.setSmallIcon(R.mipmap.ic_launcher);
            mBuilder.setContentTitle("REMINDER")
//                    .setContentText("YOU ARE INSIDE OFFICE PREMISES. YOU NEED TO"+TempActivity.status)
                    .setAutoCancel(false)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setContentIntent(resultPendingIntent);

            mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                assert mNotificationManager != null;
                mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
                mNotificationManager.createNotificationChannel(notificationChannel);

            }
            assert mNotificationManager != null;
            mNotificationManager.notify(0 /* Request Code */, mBuilder.build());
        }
        if (Double.parseDouble(distance) > 10 && TempActivity.flag == 0 && TempActivity.status.equals("CHECK-OUT")) {
            TempActivity.flag = 1;
            double d = Double.parseDouble(distance);
            int d1 = (int) d;
            Log.e("flag updated to ", "  " + TempActivity.flag);
            Toast.makeText(getApplicationContext(), "Notify me!!", Toast.LENGTH_SHORT).show();
            Intent resultIntent = new Intent(getApplicationContext(), AttendanceActivity.class);
            resultIntent.putExtra("flag", TempActivity.flag);
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(),
                    0 /* Request code */, resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder = new NotificationCompat.Builder(getApplicationContext());
            mBuilder.setSmallIcon(R.mipmap.ic_launcher);
            mBuilder.setContentTitle("REMINDER")
                    .setAutoCancel(false)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("YOU ARE " + d1 + " metres OUTSIDE OFFICE PREMISES. YOU NEED TO" + TempActivity.status))
                    .setContentIntent(resultPendingIntent);

            mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                assert mNotificationManager != null;
                mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
                mNotificationManager.createNotificationChannel(notificationChannel);
            }
            assert mNotificationManager != null;
            mNotificationManager.notify(0 /* Request Code */, mBuilder.build());
        }
        Log.d(TAG, "Sending info...");
        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_LATITUDE, lat);
        intent.putExtra(EXTRA_LONGITUDE, lng);
        intent.putExtra(EXTRA_ACCURACY, accuracy);
        intent.putExtra(EXTRA_DISTANCE, distance);
        intent.putExtra("flag", TempActivity.flag);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);


    }

    private double distance(double lat_a, double lng_a, double lat_b, double lng_b, double accuracy) {
        float pk = (float) (180.f / Math.PI);
        if (accuracy < 60) {

            double a1 = lat_a / pk;
            double a2 = lng_a / pk;
            double b1 = lat_b / pk;
            double b2 = lng_b / pk;

            double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
            double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
            double t3 = Math.sin(a1) * Math.sin(b1);
            double tt = Math.acos(t1 + t2 + t3);

            return 6366000 * tt;
        }

        return -1;

    }
}
