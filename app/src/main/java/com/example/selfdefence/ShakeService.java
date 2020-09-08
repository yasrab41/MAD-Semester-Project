package com.example.selfdefence;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;


public class ShakeService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity


    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest mLocationReq;
    LocationManager locationManager;
    int countUp,countDown;
    ArrayList<String> listTheNumbers;
    Cursor c;




    //private static final float SHAKE_THRESHOLD_GRAVITY = 4.0F;
    private static final int SHAKE_SLOP_TIME_MS = 500;
    private static final int SHAKE_COUNT_RESET_TIME_MS = 3000;
    private long mShakeTimestamp;
    private int mShakeCount;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI, new Handler());


        listTheNumbers= new ArrayList<String>();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        countUp=0;
        countDown=0;


        SQLiteDatabase db;
        db=openOrCreateDatabase("NumDB", Context.MODE_PRIVATE, null);

        c=db.rawQuery("SELECT * FROM details", null);
        while(c.moveToNext()) {
            listTheNumbers.add(c.getString(1));
        }

        return START_STICKY;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        mAccelLast = mAccelCurrent;
        mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
        float delta = mAccelCurrent - mAccelLast;
        mAccel = mAccel * 0.9f + delta; // perform low-cut filter

        if (mAccel > 11) {
            Random rnd = new Random();
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
          //  Toast.makeText(this, "Let's try it in background", Toast.LENGTH_SHORT).show();


            final long now = System.currentTimeMillis();
            // ignore shake events too close to each other (500ms)
            if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                return;
            }

            // reset the shake count after 3 seconds of no shakes
            if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                mShakeCount = 0;
            }

            mShakeTimestamp = now;
            mShakeCount++;


            if (listTheNumbers!=null) {
                if (mShakeCount == 3) {

                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + listTheNumbers.get(0)));
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permissions are denied to call", Toast.LENGTH_SHORT).show();
//                requestPermission();
                    } else {
                        startActivity(callIntent);
                    }
                    Toast.makeText(this, "Shaked!!!", Toast.LENGTH_SHORT).show();


                    int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);

                    if (permissionCheck == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        getLocation();
                        Toast.makeText(getApplicationContext(), "Message Sent successfully!", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(this, "Please Allow permission to send messag", Toast.LENGTH_SHORT).show();

                    }
                }
            }else {
                Toast.makeText(this, "Enter Contacts First", Toast.LENGTH_SHORT).show();
            }

        }
    }





    public LocationRequest getLoc() {
        LocationRequest obj = new LocationRequest();
        obj.setInterval(10000);
        obj.setFastestInterval(5000);
        obj.setPriority(obj.PRIORITY_HIGH_ACCURACY);
        return obj;
    }

    protected void getLocation() {
        fusedLocationProviderClient = new FusedLocationProviderClient(this);
        mLocationReq = getLoc();
        ArrayList<String> listOfLocation = new ArrayList<String>();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Permission are granted", Toast.LENGTH_SHORT).show();


        }

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                //initialize the location
                Location location = task.getResult();
                if (location != null) {

                    //initialize geocoder
                    Geocoder geocoder = new Geocoder(ShakeService.this, Locale.getDefault());

                    ArrayList<Address> list = new ArrayList<Address>();
                    String msg = "";

                    try {
                        list = (ArrayList) geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 10);
                        msg = "Help me I am in danger this is my location\n"+"City: " + list.get(0).getLocality()
                                + "\nComplete Address:" + list.get(0).getAddressLine(0);

                        if (!listTheNumbers.toString().isEmpty()) {
                            SmsManager smsManager = SmsManager.getDefault();
                            for (int i = 0; i < listTheNumbers.size(); i++) {
                                smsManager.sendTextMessage(listTheNumbers.get(i), null, msg, null, null);
                            }
                        } else {
                            Toast.makeText(ShakeService.this, "Please Enter phone number or message...", Toast.LENGTH_SHORT).show();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


}
