package com.example.spotme;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.database.Cursor;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "spotme.db";
    // Ensure this is set to 4 to force table recreation and apply the latest schema
    private static final int DATABASE_VERSION = 4;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("Database", "DatabaseHelper created. Version: " + DATABASE_VERSION);

        // Force database creation by getting writable database
        SQLiteDatabase db = getWritableDatabase();
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("Database", "Creating database tables...");
        createTables(db);
        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("Database", "Upgrading database from version " + oldVersion + " to " + newVersion + ". DROPPING ALL TABLES...");
        dropTables(db);
        onCreate(db);
    }

    private void dropTables(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS rides");
            db.execSQL("DROP TABLE IF EXISTS vehicles");
            db.execSQL("DROP TABLE IF EXISTS drivers");
            db.execSQL("DROP TABLE IF EXISTS passengers");
            db.execSQL("DROP TABLE IF EXISTS users");
            Log.d("Database", "All tables dropped successfully.");
        } catch (Exception e) {
            Log.e("Database", "Error dropping tables: " + e.getMessage());
        }
    }

    private void createTables(SQLiteDatabase db) {
        // 1. USERS Table
        db.execSQL("CREATE TABLE users (" +
                "user_id TEXT PRIMARY KEY," +
                "email TEXT UNIQUE NOT NULL," +
                "phone TEXT NOT NULL," +
                "name TEXT NOT NULL," +
                "user_type TEXT NOT NULL)");

        // 2. PASSENGERS Table
        db.execSQL("CREATE TABLE passengers (" +
                "passenger_id TEXT PRIMARY KEY," +
                "user_id TEXT NOT NULL," +
                "FOREIGN KEY(user_id) REFERENCES users(user_id))");

        // 3. DRIVERS Table
        db.execSQL("CREATE TABLE drivers (" +
                "driver_id TEXT PRIMARY KEY," +
                "user_id TEXT NOT NULL," +
                "license_number TEXT," +
                "is_online INTEGER DEFAULT 0," + // 0 for offline, 1 for online
                "FOREIGN KEY(user_id) REFERENCES users(user_id))");

        // 4. VEHICLES Table
        db.execSQL("CREATE TABLE vehicles (" +
                "vehicle_id TEXT PRIMARY KEY," +
                "driver_id TEXT NOT NULL," +
                "license_plate TEXT NOT NULL," +
                "make TEXT," +
                "model TEXT," +
                "year INTEGER," +
                "color TEXT," +
                "FOREIGN KEY(driver_id) REFERENCES drivers(driver_id))");

        // 5. RIDES Table
        db.execSQL("CREATE TABLE rides (" +
                "ride_id TEXT PRIMARY KEY," +
                "passenger_id TEXT NOT NULL," +
                "driver_id TEXT," +
                "pickup_address TEXT NOT NULL," +
                "status TEXT NOT NULL," + // REQUESTED, ACCEPTED, COMPLETED, CANCELLED
                "requested_at TEXT NOT NULL," +
                "FOREIGN KEY(passenger_id) REFERENCES passengers(passenger_id)," +
                "FOREIGN KEY(driver_id) REFERENCES drivers(driver_id))");
    }

    private void insertSampleData(SQLiteDatabase db) {
        Log.d("Database", "Inserting sample data...");

        // Test Passenger User
        db.execSQL("INSERT INTO users (user_id, email, phone, name, user_type) VALUES ('user1', 'passenger@test.com', '1234567890', 'Test Passenger', 'passenger')");
        db.execSQL("INSERT INTO passengers (passenger_id, user_id) VALUES ('passenger1', 'user1')");

        // Test Driver User
        db.execSQL("INSERT INTO users (user_id, email, phone, name, user_type) VALUES ('user2', 'driver@test.com', '0987654321', 'Test Driver', 'driver')");
        db.execSQL("INSERT INTO drivers (driver_id, user_id, license_number, is_online) VALUES ('driver1', 'user2', 'DRV12345', 0)");
        db.execSQL("INSERT INTO vehicles (vehicle_id, driver_id, license_plate, make, model, year, color) VALUES ('vehicle1', 'driver1', 'ABC 123', 'Toyota', 'Corolla', 2020, 'White')");

        // Optional: Pre-populate a cancelled ride for history testing
        db.execSQL("INSERT INTO rides (ride_id, passenger_id, driver_id, pickup_address, status, requested_at) VALUES ('ride_hist_1', 'passenger1', 'driver1', 'Old Street', 'COMPLETED', '1672531200000')");

        Log.d("Database", "Sample data inserted successfully.");
    }

    /**
     * Public method to force a full database reset, useful for debugging.
     */
    public void recreateDatabase() {
        Log.w("Database", "Manually forcing database recreation and sample data insertion.");
        SQLiteDatabase db = this.getWritableDatabase();
        dropTables(db);
        onCreate(db);
        db.close();
    }
    public boolean createRideRequest(String rideId, String passengerId, String pickupAddress) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            String query = "INSERT INTO rides (ride_id, passenger_id, pickup_address, status, requested_at) VALUES (" +
                    "'" + rideId + "', " +
                    "'" + passengerId + "', " +
                    "'" + pickupAddress + "', " +
                    "'REQUESTED', " +
                    "'" + System.currentTimeMillis() + "')";

            db.execSQL(query);
            return true;
        } catch (Exception e) {
            Log.e("Database", "Error: " + e.getMessage());
            return false;
        } finally {
            if (db != null) db.close();
        }
    }

    public Cursor executeQuery(String query) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            Log.d("Database", "Executing query: " + query);
            cursor = db.rawQuery(query, null);
            return cursor;
        } catch (Exception e) {
            Log.e("Database", "Query error: " + e.getMessage() + " for query: " + query);
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
    }

    public boolean executeUpdate(String query) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            Log.d("Database", "Executing update: " + query);
            db.execSQL(query);
            Log.d("Database", "Update SUCCESS");
            return true;
        } catch (Exception e) {
            Log.e("Database", "Update error: " + e.getMessage() + " for query: " + query);
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
}