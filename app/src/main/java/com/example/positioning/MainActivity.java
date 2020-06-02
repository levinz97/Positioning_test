package com.example.positioning;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.alternativevision.gpx.GPXParser;
import org.alternativevision.gpx.beans.GPX;

import java.io.FileOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection {
    Button stBtn, stopBtn, uptBtn, exitBtn;
    TextView laTxt, loTxt, disTxt, spTxt;
    Intent intent;
    BroadcastListener broadcastListener;

    private ILocationService iLocationServiceProxy = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        laTxt = findViewById(R.id.laTxt);
        loTxt = findViewById(R.id.loTxt);
        disTxt = findViewById(R.id.disTxt);
        spTxt = findViewById(R.id.spTxt);
        stBtn = findViewById(R.id.stBtn);
        stopBtn = findViewById(R.id.stopBtn);
        uptBtn = findViewById(R.id.uptBtn);
        exitBtn = findViewById(R.id.exitBtn);

        if (shouldAskPermissions()) {
            askPermissions();
        }
        broadcastListener = new BroadcastListener();
        this.registerReceiver(
               broadcastListener, new IntentFilter(LocationService.ACTION_LOCATION_BROADCAST)
        );

        uptBtn.setOnClickListener(this);
        stBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
        exitBtn.setOnClickListener(this);

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View v) {
        intent = new Intent(this,LocationService.class);
        switch (v.getId()){
            case R.id.stBtn:
                startService(intent);
                bindService(intent,this,BIND_AUTO_CREATE);
                break;
            case R.id.stopBtn:
                Log.e("Cancel", String.valueOf(isMyServiceRunning(LocationService.class)));
                laTxt.setText("Latitude: " );
                loTxt.setText("Longitude: " );
                disTxt.setText("Distance: " );
                spTxt.setText("Average Speed: " );
                if (isMyServiceRunning(LocationService.class)){
                    unbindService(this);
                }
                break;
            case R.id.exitBtn:
                stopService(intent);
                unbindService(this);
                unregisterReceiver(broadcastListener);
                finish();
                break;
            case R.id.uptBtn:
                Log.i("uptBtn","clicked");
                try {
                    iLocationServiceProxy.askUpdates();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                laTxt.setText("Latitude: " + BroadcastListener.LA);
                loTxt.setText("Longitude: " + BroadcastListener.LO);
                disTxt.setText("Distance: " + BroadcastListener.DI + "km");
                spTxt.setText("Average Speed: " + BroadcastListener.SP + "km/h");
                break;
            default:
                break;
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }



    @SuppressLint("NewApi")
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }


    protected boolean shouldAskPermissions() {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED );

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.i("connection","connected");
        iLocationServiceProxy = ILocationService.Stub.asInterface(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        iLocationServiceProxy = null;

    }
}
