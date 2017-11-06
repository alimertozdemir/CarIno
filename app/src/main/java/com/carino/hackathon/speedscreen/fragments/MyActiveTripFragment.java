package com.carino.hackathon.speedscreen.fragments;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.carino.hackathon.R;
import com.carino.hackathon.speedscreen.gauges.ColorArcProgressBar;
import com.carino.hackathon.speedscreen.model.MyTrip;
import com.carino.hackathon.speedscreen.model.Trip;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
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

public class MyActiveTripFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = MyActiveTripFragment.class.getCanonicalName();

    private VehicleManager mVehicleManager;

    private TextView mFuelConsumptionView;
    private TextView mIgnitionStatusView;

    private TextView mParkingBrakeView;
    private ImageView ivSpeed;
    private ColorArcProgressBar caProgressBar;
    private TextView tvFuelConsumption;
    private double fuelConsumption;
    private Trip trip;
    private boolean onTrip = false;
    private MyTrip myTrips = new MyTrip();
    private Gson gson = new Gson();

    private GoogleMap map;
    MapView mapView;

    private static final int PERMISSION_REQUEST_CODE_LOCATION = 1;

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

        caProgressBar = (ColorArcProgressBar) rootView.findViewById(R.id.bar1);
        tvFuelConsumption = (TextView) rootView.findViewById(R.id.tvFuelConsumption);
        mIgnitionStatusView = (TextView) rootView.findViewById(R.id.ignition_status);
        mParkingBrakeView = (TextView) rootView.findViewById(R.id.parking_brake);

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) rootView.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);


        mapView.getMapAsync(this);

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

        mapView.onPause();
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

        mapView.onResume();
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
                    mIgnitionStatusView.setText("Ignition Status : " + ignitionStatus.getValue().enumValue());
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
                    fuelConsumption = fuelConsumed.getValue().doubleValue();
                    tvFuelConsumption.setText(String.valueOf(fuelConsumed.getValue().doubleValue()));
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
                    /*TextDrawable drawable = TextDrawable.builder()
                            .beginConfig()
                            .textColor(Color.WHITE)
                            .useFont(Typeface.DEFAULT)
                            .fontSize(75)
                            .bold()
                            .toUpperCase()
                            .endConfig()
                            .buildRoundRect(String.valueOf(speed.getValue().intValue()), Color.GREEN, 180);*/
                    //ivSpeed.setImageDrawable(drawable);
                    caProgressBar.setCurrentValues((float)speed.getValue().doubleValue());
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();

            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            centerMapOnMyLocation(location);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION },
                    PERMISSION_REQUEST_CODE_LOCATION);
        }
    }

    private void centerMapOnMyLocation(Location location) {

        if (location != null)
        {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}