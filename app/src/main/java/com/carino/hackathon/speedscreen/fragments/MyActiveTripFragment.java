package com.carino.hackathon.speedscreen.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.carino.hackathon.R;
import com.carino.hackathon.speedscreen.model.MyTrip;
import com.carino.hackathon.speedscreen.model.Trip;
import com.google.gson.Gson;
import com.openxc.VehicleManager;
import com.openxc.measurements.FuelConsumed;
import com.openxc.measurements.IgnitionStatus;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.ParkingBrakeStatus;
import com.openxc.measurements.VehicleSpeed;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Date;

/**
 * Created by alimertozdemir on 4.11.2017.
 */

public class MyActiveTripFragment extends Fragment {

    private static final String TAG = MyActiveTripFragment.class.getCanonicalName();

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
    private Gson gson = new Gson();

    public MyActiveTripFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.active_trip_fragment, container, false);

        ivSpeed = (ImageView) rootView.findViewById(R.id.image_view_speed);
        ivFuelConsumption = (ImageView) rootView.findViewById(R.id.image_view_fuel);
        mFuelConsumptionView = (TextView) rootView.findViewById(R.id.fuel_consumption);
        mIgnitionStatusView = (TextView) rootView.findViewById(R.id.ignition_status);
        mParkingBrakeView = (TextView) rootView.findViewById(R.id.parking_brake);

        return rootView;
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
            getActivity().unbindService(mConnection);
            mVehicleManager = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // When the activity starts up or returns from the background,
        // re-connect to the VehicleManager so we can receive updates.
        if(mVehicleManager == null) {
            Intent intent = new Intent(getActivity(), VehicleManager.class);
            getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

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

    // Fuel Consumption
    ParkingBrakeStatus.Listener mParkingBrakeStatusListener = new ParkingBrakeStatus.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final ParkingBrakeStatus parkingBrakeStatus = (ParkingBrakeStatus) measurement;
            getActivity().runOnUiThread(new Runnable() {
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
            getActivity().runOnUiThread(new Runnable() {
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
            getActivity().runOnUiThread(new Runnable() {
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
            getActivity().runOnUiThread(new Runnable() {
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

    public void addAllListeners() {
        mVehicleManager.addListener(VehicleSpeed.class, mSpeedListener);
        mVehicleManager.addListener(FuelConsumed.class, mFuelConsumedListener);
        mVehicleManager.addListener(IgnitionStatus.class, mIgnitionStatusListener);
        mVehicleManager.addListener(ParkingBrakeStatus.class, mParkingBrakeStatusListener);
    }

    public void removeAllListeners() {
        mVehicleManager.removeListener(VehicleSpeed.class, mSpeedListener);
        mVehicleManager.removeListener(FuelConsumed.class, mFuelConsumedListener);
        mVehicleManager.removeListener(IgnitionStatus.class, mIgnitionStatusListener);
        mVehicleManager.removeListener(ParkingBrakeStatus.class, mParkingBrakeStatusListener);
    }

}