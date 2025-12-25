package com.example.spotme;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.ArrayList;
import java.util.List;

public class DriverHomeActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private String driverId;
    private RecyclerView rvRideRequests;
    private RideRequestsAdapter adapter;
    private List<Ride> requestList = new ArrayList<>();
    private TextView tvStatusLabel;

    // Polling Logic
    private Handler pollingHandler = new Handler(Looper.getMainLooper());
    private Runnable pollingRunnable;
    private boolean isOnline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);

        dbHelper = new DatabaseHelper(this);
        loadDriverId();

        tvStatusLabel = findViewById(R.id.tvStatusLabel);
        rvRideRequests = findViewById(R.id.rvRideRequests);
        rvRideRequests.setLayoutManager(new LinearLayoutManager(this));

        // Setup Adapter
        adapter = new RideRequestsAdapter(requestList, new RideRequestsAdapter.OnRequestActionListener() {
            @Override
            public void onAcceptClicked(Ride ride) {
                acceptRide(ride);
            }

            @Override
            public void onRejectClicked(Ride ride) {
                requestList.remove(ride);
                adapter.notifyDataSetChanged();
            }
        });
        rvRideRequests.setAdapter(adapter);

        // Define the Polling Task
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (isOnline) {
                    fetchAvailableRides();
                    pollingHandler.postDelayed(this, 3000); // Repeat every 3 seconds
                }
            }
        };

        SwitchMaterial switchOnline = findViewById(R.id.switchOnline);
        switchOnline.setOnCheckedChangeListener((button, checked) -> {
            isOnline = checked;
            updateOnlineStatusInDb(checked);

            if (checked) {
                tvStatusLabel.setText("Online & Searching...");
                tvStatusLabel.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                pollingHandler.post(pollingRunnable); // Start polling
            } else {
                tvStatusLabel.setText("Offline");
                tvStatusLabel.setTextColor(getResources().getColor(android.R.color.darker_gray));
                pollingHandler.removeCallbacks(pollingRunnable); // Stop polling
                requestList.clear();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void fetchAvailableRides() {
        // Query only rides with status 'REQUESTED'
        String query = "SELECT r.ride_id, r.pickup_address, r.requested_at, u.name " +
                "FROM rides r JOIN passengers p ON r.passenger_id = p.passenger_id " +
                "JOIN users u ON p.user_id = u.user_id " +
                "WHERE r.status = 'REQUESTED'";

        Cursor cursor = dbHelper.executeQuery(query);
        List<Ride> newRides = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Ride ride = new Ride();
                ride.setRideId(cursor.getString(0));
                ride.setPickupAddress(cursor.getString(1));
                ride.setRequestedAt(cursor.getString(2));
                ride.setPassengerName(cursor.getString(3));
                newRides.add(ride);
            }
            cursor.close();
        }

        // Update UI only if data changed
        requestList.clear();
        requestList.addAll(newRides);
        adapter.notifyDataSetChanged();
    }

    private void acceptRide(Ride ride) {
        String query = "UPDATE rides SET status = 'ACCEPTED', driver_id = '" + driverId +
                "' WHERE ride_id = '" + ride.getRideId() + "'";
        if (dbHelper.executeUpdate(query)) {
            Toast.makeText(this, "Ride Accepted!", Toast.LENGTH_SHORT).show();
            fetchAvailableRides(); // Immediate refresh
        }
    }

    private void updateOnlineStatusInDb(boolean online) {
        int val = online ? 1 : 0;
        dbHelper.executeUpdate("UPDATE drivers SET is_online = " + val + " WHERE driver_id = '" + driverId + "'");
    }

    private void loadDriverId() {
        SharedPreferences prefs = getSharedPreferences("SpotMePrefs", MODE_PRIVATE);
        driverId = prefs.getString("profileId", "driver1");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pollingHandler.removeCallbacks(pollingRunnable); // Prevent memory leaks
    }
}