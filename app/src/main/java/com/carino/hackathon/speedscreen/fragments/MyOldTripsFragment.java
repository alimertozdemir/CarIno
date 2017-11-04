package com.carino.hackathon.speedscreen.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.carino.hackathon.R;
import com.carino.hackathon.speedscreen.fragments.adaptor.OldTripsAdaptor;
import com.carino.hackathon.speedscreen.fragments.adaptor.RecyclerItemDecoration;
import com.carino.hackathon.speedscreen.model.MyTrip;
import com.carino.hackathon.speedscreen.model.Trip;
import com.google.gson.Gson;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alimertozdemir on 4.11.2017.
 */

public class MyOldTripsFragment extends Fragment {

    private OldTripsAdaptor oldTripsAdaptor;
    private RecyclerView rvOldTrips;
    private Gson gson = new Gson();

    private List<Trip> myTrips = new ArrayList<Trip>();

    public MyOldTripsFragment() {
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
        View rootView = inflater.inflate(R.layout.my_old_trips_fragment, container, false);
        rvOldTrips = (RecyclerView) rootView.findViewById(R.id.rv_old_trips);
        rvOldTrips.addItemDecoration(new RecyclerItemDecoration(getActivity()));

        String trips = Prefs.getString("TRIPS", null);
        if(trips != null) {
            MyTrip trip = gson.fromJson(trips, MyTrip.class);
            myTrips = trip.getMyTrips();
        }
        oldTripsAdaptor = new OldTripsAdaptor(myTrips);
        rvOldTrips.setAdapter(oldTripsAdaptor);
        return rootView;
    }

}