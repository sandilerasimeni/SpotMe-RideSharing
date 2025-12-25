package com.example.spotme;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.ViewHolder> {
    private List<Ride> rideRequests;
    private OnRideAcceptListener acceptListener;

    /**
     * Error 1 & 3 Fix: This interface is required by DriverHomeActivity.
     */
    public interface OnRideAcceptListener {
        void onRideAccept(Ride ride);
    }

    /**
     * Error 2 Fix: This two-argument constructor is required by DriverHomeActivity.
     */
    public RideAdapter(List<Ride> rideRequests, OnRideAcceptListener acceptListener) {
        this.rideRequests = rideRequests;
        this.acceptListener = acceptListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Assuming your layout file for a single ride item is named item_ride_request.xml
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ride_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ride ride = rideRequests.get(position);

        // Set passenger name
        String passengerName = ride.getPassengerName();
        holder.tvPassengerName.setText(passengerName != null ? passengerName : "Passenger");

        // Set pickup address
        String pickupAddress = ride.getPickupAddress();
        holder.tvPickupAddress.setText(pickupAddress != null ? pickupAddress : "Location not specified");

        // Set request time
        String requestTime = ride.getRequestedAt();
        holder.tvRequestTime.setText(requestTime != null ? requestTime : "Just now");

        // Attach the click listener
        holder.btnAccept.setOnClickListener(v -> {
            if (acceptListener != null) {
                acceptListener.onRideAccept(ride);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rideRequests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvPassengerName, tvPickupAddress, tvRequestTime;
        public Button btnAccept;

        public ViewHolder(View view) {
            super(view);
            tvPassengerName = view.findViewById(R.id.tvPassengerName);
            tvPickupAddress = view.findViewById(R.id.tvPickupAddress);
            tvRequestTime = view.findViewById(R.id.tvRequestTime);
            btnAccept = view.findViewById(R.id.btnAccept);
        }
    }
}