package com.example.spotme;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DebugActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private TextView tvDebug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        tvDebug = findViewById(R.id.tvDebug);
        Button btnCheckDb = findViewById(R.id.btnCheckDb);
        Button btnRecreateDb = findViewById(R.id.btnRecreateDb);
        Button btnCreateTestRide = findViewById(R.id.btnCreateTestRide);

        dbHelper = new DatabaseHelper(this);

        btnCheckDb.setOnClickListener(v -> checkDatabase());
        btnRecreateDb.setOnClickListener(v -> recreateDatabase());
        btnCreateTestRide.setOnClickListener(v -> createTestRide());

        checkDatabase();
    }

    private void checkDatabase() {
        StringBuilder debugInfo = new StringBuilder();
        debugInfo.append("=== DATABASE DEBUG ===\n\n");

        // Check all tables
        String[] tables = {"users", "passengers", "drivers", "rides"};

        for (String table : tables) {
            debugInfo.append("=== ").append(table.toUpperCase()).append(" ===\n");
            String query = "SELECT * FROM " + table;
            android.database.Cursor cursor = dbHelper.executeQuery(query);

            if (cursor != null) {
                debugInfo.append("Rows: ").append(cursor.getCount()).append("\n");
                String[] columns = cursor.getColumnNames();
                debugInfo.append("Columns: ").append(java.util.Arrays.toString(columns)).append("\n");

                if (cursor.moveToFirst()) {
                    do {
                        debugInfo.append("ROW: ");
                        for (String col : columns) {
                            try {
                                String value = cursor.getString(cursor.getColumnIndexOrThrow(col));
                                debugInfo.append(col).append("=").append(value).append(" | ");
                            } catch (Exception e) {
                                debugInfo.append(col).append("=ERROR | ");
                            }
                        }
                        debugInfo.append("\n");
                    } while (cursor.moveToNext());
                } else {
                    debugInfo.append("TABLE IS EMPTY\n");
                }
                cursor.close();
            } else {
                debugInfo.append("TABLE DOESN'T EXIST OR ERROR\n");
            }
            debugInfo.append("\n");
        }

        tvDebug.setText(debugInfo.toString());
        Log.d("DebugActivity", debugInfo.toString());
    }

    private void recreateDatabase() {
        dbHelper.recreateDatabase();
        checkDatabase();
    }

    private void createTestRide() {
        // Create a test ride manually
        String rideId = "debug_ride_" + System.currentTimeMillis();
        String query = "INSERT INTO rides (ride_id, passenger_id, status, pickup_address) VALUES (" +
                "'" + rideId + "', " +
                "'pass1', " +
                "'requested', " +
                "'Test Location')";

        if (dbHelper.executeUpdate(query)) {
            Log.d("DebugActivity", "✅ Test ride created: " + rideId);
            checkDatabase();
        } else {
            Log.e("DebugActivity", "❌ Failed to create test ride");
        }
    }
}