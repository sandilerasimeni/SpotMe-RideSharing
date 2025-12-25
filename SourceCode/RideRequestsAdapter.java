package com.example.spotme;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RideRequestsAdapter extends RecyclerView.Adapter<RideRequestsAdapter.RequestViewHolder> {

    // Same interface from previous correction
    public interface OnRequestActionListener {
        void onAcceptClicked(Ride ride);
        void onRejectClicked(Ride ride);
    }

    private List<Ride> requestList;
    private OnRequestActionListener callback;

    public RideRequestsAdapter(List<Ride> requestList, OnRequestActionListener callback) {
        this.requestList = requestList;
        this.callback = callback;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Uses the fixed item_ride_request.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ride_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Ride ride = requestList.get(position);
        // Correctly using tvPassengerName and tvRequestTime now
        holder.tvPassengerName.setText("Passenger: " + (ride.getPassengerName() != null ? ride.getPassengerName() : "Unknown"));
        holder.tvPickupAddress.setText("Pickup: " + ride.getPickupAddress());
        holder.tvRequestTime.setText("Requested at: " + ride.getRequestedAt());

        holder.btnAccept.setOnClickListener(v -> callback.onAcceptClicked(ride));
        // Mapped to the new btnReject in the fixed XML
        holder.btnReject.setOnClickListener(v -> callback.onRejectClicked(ride));
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        public TextView tvPassengerName; // Renamed to match XML
        public TextView tvPickupAddress;
        public TextView tvRequestTime; // Renamed to match XML
        public Button btnAccept;
        public Button btnReject; // Added to match the fixed XML

        public RequestViewHolder(View itemView) {
            super(itemView);
            // Mapped to the IDs in item_ride_request.xml
            tvPassengerName = itemView.findViewById(R.id.tvPassengerName);
            tvPickupAddress = itemView.findViewById(R.id.tvPickupAddress);
            tvRequestTime = itemView.findViewById(R.id.tvRequestTime);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject); // Mapped the new button
        }
    }
}