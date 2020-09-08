package com.example.selfdefence;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class TrigActivity extends AppCompatActivity {

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest mLocationReq;
    LocationManager locationManager;
    int countUp,countDown;
    ArrayList<String> listTheNumbers;
    Cursor c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trig);


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


        Intent i_startservice=new Intent(this,ShakeService.class);
        startService(i_startservice);

        locationEnabled();
    }

    public void emergencyCall(View view) {

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + listTheNumbers.get(0)));
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permissions are denied to call", Toast.LENGTH_SHORT).show();
//                requestPermission();
        } else {
            startActivity(callIntent);
        }
    }

    public void emergencySmsLoc(View view) {
        getLocation();
    }

    public void emergencySms(View view) {
        String msg = "Please Help me I am in danger..";
        SmsManager smsManager=SmsManager.getDefault();
        for (int i = 0; i < listTheNumbers.size(); i++) {
            smsManager.sendTextMessage(listTheNumbers.get(i), null, msg, null, null);
        }
        Toast.makeText(getApplicationContext(), "Message Sent successfully!", Toast.LENGTH_LONG).show();
    }







    public boolean dispatchKeyEvent(KeyEvent event) {

        int action,keycode;

        action = event.getAction(); //we get action while user is pressing a volume button or releasing a volume button
        keycode=event.getKeyCode(); //keycode is used to know which button is pressing volume low or volume down


        switch(keycode){
            case KeyEvent.KEYCODE_VOLUME_UP:
            {
                if(KeyEvent.ACTION_UP==action){
                    countUp++;
                    String num = String.valueOf(countUp);

                    if(countUp==5){
                        countUp=0;

                        // for phone call
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + listTheNumbers.get(0)));
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                        {
                            Toast.makeText(this, "Permissions are denied to call", Toast.LENGTH_SHORT).show();
                            requestPermission();
                        } else {
                            startActivity(callIntent);
                        }

                        //for Sms
                        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
                        if(permissionCheck == PackageManager.PERMISSION_GRANTED     &&    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                            locationEnabled();
                            getLocation();
                        }
                        else {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 0);
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                        }


                    } else {
                      //  Toast.makeText(this, "Up Key Pressed: "+countUp, Toast.LENGTH_SHORT).show();
                    }

                }
                break;
            }
            case KeyEvent.KEYCODE_VOLUME_DOWN: // for volumn down key
                if(KeyEvent.ACTION_DOWN==action){

                    countDown++;

                    if (countDown==5) {
                        countDown=0;
                        countUp=0;

                        String msg = "Please Help me I am in danger..";
                        SmsManager smsManager=SmsManager.getDefault();
                        for (int i = 0; i < listTheNumbers.size(); i++) {
                            smsManager.sendTextMessage(listTheNumbers.get(i), null, msg, null, null);
                        }
                       // Toast.makeText(getApplicationContext(), "Message Sent successfully!", Toast.LENGTH_LONG).show();
                    }
                    else{
                       // Toast.makeText(this,"Down Key Pressed: "+countDown,Toast.LENGTH_SHORT).show();
                    }
                }
        }
        return super.dispatchKeyEvent(event);

    }


    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]
                {
                        Manifest.permission.CALL_PHONE
                }, 8);

    }


    private void locationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(TrigActivity.this)
                    .setTitle("Enable GPS Service")
                    .setMessage("We need your GPS location to show Near Places around you.")
                    .setCancelable(false)
                    .setPositiveButton("Enable", new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            })
                    .setNegativeButton("Cancel", null)
                    .show();
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
//            ActivityCompat.requestPermissions(getApplicationContext(), new String[]{
//                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            Toast.makeText(this, "Permission are granted", Toast.LENGTH_SHORT).show();


        }

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                //initialize the location
                Location location = task.getResult();
                if (location != null) {
                    Geocoder geocoder = new Geocoder(TrigActivity.this, Locale.getDefault());
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
                            Toast.makeText(TrigActivity.this, "Please Enter phone number or message...", Toast.LENGTH_SHORT).show();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


}