package com.example.spotme;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PassengerHomeActivity extends AppCompatActivity {
    private Button btnRequestRide, btnCancelRide;
    private TextView tvStatus;
    private DatabaseHelper dbHelper;
    private String passengerId;

    // Variables for ride history/status display
    private RecyclerView rvRideHistory;
    private RideAdapter rideAdapter;
    private List<Ride> rideHistoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_home);

        Log.d("PassengerHome", "=== STARTING PASSENGER HOME ===");

        // 1. Load IDs and Initialize the Database FIRST
        loadPassengerId();
        initializeDatabase(); // <--- Moved up!

        // 2. Initialize UI views
        initializeViews();

        // 3. Setup RecyclerView (which now has a valid dbHelper to use)
        setupRideHistoryRecyclerView();

        // 4. Setup listeners and logic
        setupClickListeners();
        checkActiveRide();
    }

    private void initializeViews() {
        btnRequestRide = findViewById(R.id.btnRequestRide);
        btnCancelRide = findViewById(R.id.btnCancelRide);
        tvStatus = findViewById(R.id.tvStatus);
        rvRideHistory = findViewById(R.id.rvRideHistory);
    }

    private void setupClickListeners() {
        // UPDATED: Now navigates to the request screen you built earlier
        btnRequestRide.setOnClickListener(v -> {
            Intent intent = new Intent(this, RequestRideActivity.class);
            startActivity(intent);
        });

        btnCancelRide.setOnClickListener(v -> cancelRide());
    }

    private void cancelRide() {
        if (passengerId == null) return;

        // Use parameterized query to avoid SQL syntax errors with strings
        String query = "UPDATE rides SET status = 'CANCELLED' WHERE passenger_id = ? AND status IN ('REQUESTED', 'ACCEPTED')";

        // Using the safe update method from DatabaseHelper
        boolean success = dbHelper.executeUpdate(query.replace("?", "'" + passengerId + "'"));

        if (success) {
            runOnUiThread(() -> {
                tvStatus.setText("Ready to ride");
                btnRequestRide.setEnabled(true);
                btnCancelRide.setEnabled(false);
                Toast.makeText(this, "Ride cancelled", Toast.LENGTH_SHORT).show();
                loadRideHistory();
            });
        }
    }

    // Method to load passenger ID from SharedPreferences
    private void loadPassengerId() {
        SharedPreferences prefs = getSharedPreferences("SpotMePrefs", MODE_PRIVATE);
        passengerId = prefs.getString("profileId", null);
        if (passengerId == null) {
            Toast.makeText(this, "Error: Passenger ID not found in session.", Toast.LENGTH_LONG).show();
            Log.e("PassengerHome", "Passenger ID not found. Finishing activity.");
            finish();
            return;
        }
        Log.d("PassengerHome", "Passenger ID loaded from session: " + passengerId);
    }

    // FIX: Provides the required two-argument constructor for RideAdapter
    private void setupRideHistoryRecyclerView() {
        // Provide a dummy listener implementation for the RideAdapter constructor
        rideAdapter = new RideAdapter(rideHistoryList, new RideAdapter.OnRideAcceptListener() {
            @Override
            public void onRideAccept(Ride ride) {
                // Do nothing: This method is unused in the Passenger view.
                Log.w("PassengerHome", "Attempted to accept ride, ignoring.");
            }
        });

        rvRideHistory.setLayoutManager(new LinearLayoutManager(this));
        rvRideHistory.setAdapter(rideAdapter);

        // Initial load of history
        loadRideHistory();
    }


    private void initializeDatabase() {
        try {
            dbHelper = new DatabaseHelper(this);
            Log.d("PassengerHome", "Database helper created");
        } catch (Exception e) {
            Log.e("PassengerHome", "Database init error: " + e.getMessage());
            Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void requestRide() {
        if (passengerId == null) return;

        // CRITICAL: Ensure no active ride exists before requesting a new one
        String checkQuery = "SELECT COUNT(*) FROM rides WHERE passenger_id = '" + passengerId + "' AND status IN ('REQUESTED', 'ACCEPTED')";
        Cursor checkCursor = dbHelper.executeQuery(checkQuery);
        if (checkCursor != null && checkCursor.moveToFirst() && checkCursor.getInt(0) > 0) {
            Toast.makeText(this, "You already have an active ride.", Toast.LENGTH_SHORT).show();
            checkCursor.close();
            return;
        }
        if (checkCursor != null) checkCursor.close();

        // Mock data for the new ride request
        String newRideId = UUID.randomUUID().toString();
        String pickupAddress = "Passenger's Current Location";
        String requestedAt = String.valueOf(System.currentTimeMillis());

        String insertQuery = "INSERT INTO rides (ride_id, passenger_id, driver_id, pickup_address, status, requested_at) VALUES ('" +
                newRideId + "', '" +
                passengerId + "', " +
                "NULL" + ", '" +
                pickupAddress + "', '" +
                "REQUESTED" + "', '" +
                requestedAt + "')";

        if (dbHelper.executeUpdate(insertQuery)) {
            tvStatus.setText("Looking for driver...");
            btnRequestRide.setEnabled(false);
            btnCancelRide.setEnabled(true);
            Toast.makeText(this, "Ride requested!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to request ride.", Toast.LENGTH_SHORT).show();
        }
    }
    private void checkActiveRide() {
        if (passengerId == null) return;

        String query = "SELECT status FROM rides WHERE passenger_id = '" + passengerId + "' AND status IN ('REQUESTED', 'ACCEPTED') LIMIT 1";
        android.database.Cursor cursor = dbHelper.executeQuery(query);

        if (cursor != null && cursor.moveToFirst()) {
            String status = cursor.getString(0);
            Log.d("PassengerHome", "Active ride found: " + status);
            runOnUiThread(() -> {
                tvStatus.setText(status.equals("REQUESTED") ? "Looking for driver..." : "Driver on the way!");
                btnRequestRide.setEnabled(false);
                btnCancelRide.setEnabled(true);
            });
            cursor.close();
        } else {
            Log.d("PassengerHome", "No active rides found");
            runOnUiThread(() -> {
                tvStatus.setText("Ready to ride");
                btnRequestRide.setEnabled(true);
                btnCancelRide.setEnabled(false);
            });
        }
        if (cursor != null) cursor.close();
    }

    private void loadRideHistory() {
        if (passengerId == null) return;

        Log.d("PassengerHome", "Loading ride history...");

        // Query to get completed/cancelled rides
        String query = "SELECT r.ride_id, r.pickup_address, r.requested_at, u.name " +
                "FROM rides r " +
                "LEFT JOIN drivers d ON r.driver_id = d.driver_id " +
                "LEFT JOIN users u ON d.user_id = u.user_id " +
                "WHERE r.passenger_id = '" + passengerId + "' AND r.status IN ('COMPLETED', 'CANCELLED') " +
                "ORDER BY r.requested_at DESC";

        android.database.Cursor cursor = dbHelper.executeQuery(query);
        List<Ride> history = new ArrayList<>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Ride ride = new Ride();
                ride.setRideId(cursor.getString(0));
                ride.setPickupAddress(cursor.getString(1));
                ride.setRequestedAt(cursor.getString(2));
                // Get driver name or status
                String driverName = cursor.getString(3);
                if (driverName != null) {
                    // Re-use passengerName field in Ride class to display the driver's name in history
                    ride.setPassengerName("Driver: " + driverName);
                } else {
                    ride.setPassengerName("No Driver");
                }
                history.add(ride);
            }
            cursor.close();
        }

        // Update the RecyclerView on the main thread
        runOnUiThread(() -> {
            rideHistoryList.clear();
            rideHistoryList.addAll(history);
            if (rideAdapter != null) {
                rideAdapter.notifyDataSetChanged();
                Log.d("PassengerHome", "Loaded " + rideHistoryList.size() + " history items.");
            } else {
                Log.e("PassengerHome", "Adapter is null - cannot update UI");
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkActiveRide();
    }
}