package com.example.spotme;

import java.util.Objects;

public class Ride {
    private String rideId;
    private String passengerId;    // Required by DriverHomeActivity logic
    private String driverId;       // Required for database update
    private String status;         // Required for database update and display
    private String passengerName;  // Required for Driver request display
    private String driverName;     // Required for Passenger history display
    private String pickupAddress;
    private String requestedAt;

    public Ride() {}

    // Getters and Setters for ALL properties
    public String getRideId() { return rideId; }
    public void setRideId(String rideId) { this.rideId = rideId; }

    // --- FIXES for Errors 4, 6, 7 (get/set PassengerId) ---
    public String getPassengerId() { return passengerId; }
    public void setPassengerId(String passengerId) { this.passengerId = passengerId; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    // --- FIXES for Errors 5, 8, 14 (get/set Status) ---
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

    // --- FIXES for Error 9, 12, 13 (get/set DriverName) ---
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public String getRequestedAt() { return requestedAt; }
    public void setRequestedAt(String requestedAt) { this.requestedAt = requestedAt; }

    // Overridden equals/hashCode for safe list comparison in DriverHomeActivity
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ride ride = (Ride) o;
        return Objects.equals(rideId, ride.rideId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rideId);
    }
}