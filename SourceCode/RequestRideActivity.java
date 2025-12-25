package com.example.spotme;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.UUID;

public class RequestRideActivity extends AppCompatActivity {

    private TextInputEditText etPickupLocation, etDestination;
    private TextView tvEstimatedFare;
    private MaterialButton btnConfirmRide, btnCancel;
    private DatabaseHelper dbHelper;
    private String passengerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure your XML file is named activity_ride_request.xml
        setContentView(R.layout.activity_ride_request);

        dbHelper = new DatabaseHelper(this);
        loadPassengerSession();
        initializeViews();
        setupClickListeners();
    }

    private void loadPassengerSession() {
        SharedPreferences prefs = getSharedPreferences("SpotMePrefs", MODE_PRIVATE);
        // We use 'profileId' which was saved during login (this is the passenger_id)
        passengerId = prefs.getString("profileId", null);

        if (passengerId == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        // These IDs now match your new CoordinatorLayout XML exactly
        etPickupLocation = findViewById(R.id.etPickupLocation);
        etDestination = findViewById(R.id.etDestination);
        tvEstimatedFare = findViewById(R.id.tvEstimatedFare);
        btnConfirmRide = findViewById(R.id.btnConfirmRide);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupClickListeners() {
        btnConfirmRide.setOnClickListener(v -> handleRideRequest());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void handleRideRequest() {
        String pickup = etPickupLocation.getText().toString().trim();
        String dest = etDestination.getText().toString().trim();
        String fullPath = pickup + " to " + dest;

        if (pickup.isEmpty() || dest.isEmpty()) {
            Toast.makeText(this, "Please enter a destination", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a unique ride ID
        String rideId = "ride_" + UUID.randomUUID().toString().substring(0, 8);

        // Call the DatabaseHelper method (ensure it matches the Version 4 schema)
        if (dbHelper.createRideRequest(rideId, passengerId, fullPath)) {
            Log.d("RequestRide", "✓ Ride successfully created in DB: " + rideId);
            Toast.makeText(this, "Searching for nearby drivers...", Toast.LENGTH_LONG).show();
            finish(); // Return to PassengerHome
        } else {
            Log.e("RequestRide", "❌ Database insertion failed");
            Toast.makeText(this, "Request failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}