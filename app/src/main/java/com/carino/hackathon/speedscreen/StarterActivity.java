package com.carino.hackathon.speedscreen;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.carino.hackathon.R;
import com.carino.hackathon.speedscreen.model.MyTrip;
import com.carino.hackathon.speedscreen.model.Trip;
import com.carino.hackathon.speedscreen.utils.ApplicationUtility;
import com.google.gson.Gson;
import com.openxc.VehicleManager;
import com.openxc.measurements.FuelConsumed;
import com.openxc.measurements.IgnitionStatus;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.ParkingBrakeStatus;
import com.openxc.measurements.VehicleSpeed;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StarterActivity extends AppCompatActivity {
    private static final String TAG = StarterActivity.class.getCanonicalName();

    private VehicleManager mVehicleManager;

    private TextView mFuelConsumptionView;
    private TextView mIgnitionStatusView;
    private TextView mParkingBrakeView;
    private ImageView ivSpeed;
    private ImageView ivFuelConsumption;
    private double fuelConsumption;
    private Trip trip;
    private boolean onTrip = false;
    private MyTrip myTrips = new MyTrip();
    private Toolbar toolbar;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);
        // Set Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        // grab a reference to the engine speed text object in the UI, so we can
        // manipulate its value later from Java code
        ivSpeed = (ImageView) findViewById(R.id.image_view_speed);
        ivFuelConsumption = (ImageView) findViewById(R.id.image_view_fuel);
        mFuelConsumptionView = (TextView) findViewById(R.id.fuel_consumption);
        mIgnitionStatusView = (TextView) findViewById(R.id.ignition_status);
        mParkingBrakeView = (TextView) findViewById(R.id.parking_brake);

        ApplicationUtility.initializePrefs();

        String trips = Prefs.getString("TRIPS", null);
        Log.d(TAG, "MY TRIP JSON : " + trips);
    }

    @Override
    public void onPause() {
        super.onPause();
        // When the activity goes into the background or exits, we want to make
        // sure to unbind from the service to avoid leaking memory
        if(mVehicleManager != null) {
            Log.i(TAG, "Unbinding from Vehicle Manager");
            // Remember to remove your listeners, in typical Android
            // fashion.
            removeAllListeners();
            unbindService(mConnection);
            mVehicleManager = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // When the activity starts up or returns from the background,
        // re-connect to the VehicleManager so we can receive updates.
        if(mVehicleManager == null) {
            Intent intent = new Intent(this, VehicleManager.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    // Fuel Consumption
     ParkingBrakeStatus.Listener mParkingBrakeStatusListener = new ParkingBrakeStatus.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final ParkingBrakeStatus parkingBrakeStatus = (ParkingBrakeStatus) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mParkingBrakeView.setText("Parking Brake Level : " + parkingBrakeStatus.getValue().booleanValue());
                }
            });
        }
    };

    // Fuel Consumption
    IgnitionStatus.Listener mIgnitionStatusListener = new IgnitionStatus.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final IgnitionStatus ignitionStatus = (IgnitionStatus) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {

                    if(IgnitionStatus.IgnitionPosition.START.equals(ignitionStatus.getValue().enumValue())) {

                        if (!onTrip) {
                            Log.d(TAG, "TRIP STARTED AT DATE : " + new Date().toString());
                            trip = new Trip();
                            trip.setStartDate(new Date());
                            onTrip = true;
                        }

                    }
                    if(IgnitionStatus.IgnitionPosition.OFF.equals(ignitionStatus.getValue().enumValue())) {

                        if(onTrip) {
                            Log.d(TAG, "TRIP FINISHED AT DATE : " + new Date().toString());
                            trip.setFinishDate(new Date());
                            trip.setFuelConsumption(fuelConsumption);
                            String trips = Prefs.getString("TRIPS", null);
                            if(trips != null) {
                                myTrips = (MyTrip) gson.fromJson(trips, MyTrip.class);
                                myTrips.getMyTrips().add(trip);
                                Prefs.putString("TRIPS", myTrips.toJson());
                            } else {
                                myTrips.getMyTrips().add(trip);
                                Prefs.putString("TRIPS", myTrips.toJson());
                            }
                            onTrip = false;

                        }
                    }
                    //mIgnitionStatusView.setText("Ignition Status : " + );
                }
            });
        }
    };

    // Fuel Consumption
    FuelConsumed.Listener mFuelConsumedListener = new FuelConsumed.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final FuelConsumed fuelConsumed = (FuelConsumed) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    //TextDrawable drawable = TextDrawable.builder()
                            //.buildRect(String.valueOf(fuelConsumed.getValue().doubleValue()), Color.RED);
                    TextDrawable drawable = TextDrawable.builder()
                            .beginConfig()
                            .textColor(Color.WHITE)
                            .useFont(Typeface.DEFAULT)
                            .fontSize(30) /* size in px */
                            .bold()
                            .toUpperCase()
                            .endConfig()
                            .buildRect(String.valueOf(fuelConsumed.getValue().doubleValue()), Color.RED);
                    ivFuelConsumption.setImageDrawable(drawable);
                    fuelConsumption = fuelConsumed.getValue().doubleValue();
                    //mFuelConsumptionView.setText("Total Consumption (L): " + fuelConsumed.getValue().doubleValue());
                }
            });
        }
    };

    // Vehicle Speed
    VehicleSpeed.Listener mSpeedListener = new VehicleSpeed.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final VehicleSpeed speed = (VehicleSpeed) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    TextDrawable drawable = TextDrawable.builder()
                            .beginConfig()
                            .textColor(Color.WHITE)
                            .useFont(Typeface.DEFAULT)
                            .fontSize(75) /* size in px */
                            .bold()
                            .toUpperCase()
                            .endConfig()
                            .buildRoundRect(String.valueOf(speed.getValue().intValue()), Color.GREEN, 180);
                    ivSpeed.setImageDrawable(drawable);
                }
            });
        }
    };


    // Connection
    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the VehicleManager service is
        // established, i.e. bound.
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            // When the VehicleManager starts up, we store a reference to it
            // here in "mVehicleManager" so we can call functions on it
            // elsewhere in our code.
            mVehicleManager = ((VehicleManager.VehicleBinder) service).getService();

            // We want to receive updates whenever the EngineSpeed changes. We
            // have an EngineSpeed.Listener (see above, mSpeedListener) and here
            // we request that the VehicleManager call its receive() method
            // whenever the EngineSpeed changes
            addAllListeners();
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleManager Service  disconnected unexpectedly");
            mVehicleManager = null;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.starter, menu);
        return true;
    }

    private void addAllListeners() {
        mVehicleManager.addListener(VehicleSpeed.class, mSpeedListener);
        mVehicleManager.addListener(FuelConsumed.class, mFuelConsumedListener);
        mVehicleManager.addListener(IgnitionStatus.class, mIgnitionStatusListener);
        mVehicleManager.addListener(ParkingBrakeStatus.class, mParkingBrakeStatusListener);
    }

    private void removeAllListeners() {
        mVehicleManager.removeListener(VehicleSpeed.class, mSpeedListener);
        mVehicleManager.removeListener(FuelConsumed.class, mFuelConsumedListener);
        mVehicleManager.removeListener(IgnitionStatus.class, mIgnitionStatusListener);
        mVehicleManager.removeListener(ParkingBrakeStatus.class, mParkingBrakeStatusListener);
    }
}
