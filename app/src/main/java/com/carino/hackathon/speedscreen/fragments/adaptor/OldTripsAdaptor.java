package com.carino.hackathon.speedscreen.fragments.adaptor;

import android.graphics.Movie;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.carino.hackathon.R;
import com.carino.hackathon.speedscreen.model.Trip;
import com.carino.hackathon.speedscreen.utils.ApplicationUtility;

import java.util.List;

/**
 * Created by alimertozdemir on 4.11.2017.
 */

public class OldTripsAdaptor extends RecyclerView.Adapter<OldTripsAdaptor.ResultHolder> {

    private List<Trip> myTrips;
    private OnItemClickListener onItemClickListener;

    public class ResultHolder extends RecyclerView.ViewHolder {

        private TextView startTime;
        private TextView finishTime;
        private TextView startLocation;
        private TextView finishLocation;

        public ResultHolder(View view) {
            super(view);

            startTime = view.findViewById(R.id.start_time);
            finishTime = view.findViewById(R.id.finish_time);
            startLocation = view.findViewById(R.id.start_location);
            finishLocation = view.findViewById(R.id.finished_location);
        }

    }

    public OldTripsAdaptor(List<Trip> myTrips) {
        this.myTrips = myTrips;
    }

    @Override
    public OldTripsAdaptor.ResultHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip, parent, false);

        return new OldTripsAdaptor.ResultHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final OldTripsAdaptor.ResultHolder holder, int position) {

        final Trip item = myTrips.get(position);

        String startDate = ApplicationUtility.getHourAndMinutesFromDate(item.getStartDate());
        holder.startTime.setText(startDate);
        String finishedDate = ApplicationUtility.getHourAndMinutesFromDate(item.getFinishDate());
        holder.finishTime.setText(finishedDate);
        holder.startLocation.setText(item.getStartLocation());
        holder.finishLocation.setText(item.getStoppedLocation());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //onItemClickListener.onClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (myTrips == null)
            return 0;

        return myTrips.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void updateTrips(List<Trip> myTrips) {
        this.myTrips = myTrips;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onClick(Trip item);
    }

}