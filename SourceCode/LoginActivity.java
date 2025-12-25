package com.example.spotme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.UUID;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private MaterialCardView passengerCard, driverCard;
    private MaterialButton btnLogin;
    private TextView btnRegister; // Linked to the "Register" link in footer
    private MaterialButton btnGoogle;
    private DatabaseHelper dbHelper;
    private String selectedUserType = "passenger";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginn);

        dbHelper = new DatabaseHelper(this);
        initializeViews();
        setupClickListeners();

        // Default state
        setRoleSelection("passenger");
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        passengerCard = findViewById(R.id.passengerCard);
        driverCard = findViewById(R.id.driverCard);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegisterLink); // Matches XML ID
        btnGoogle = findViewById(R.id.btnGoogle);
    }

    private void setupClickListeners() {
        passengerCard.setOnClickListener(v -> setRoleSelection("passenger"));
        driverCard.setOnClickListener(v -> setRoleSelection("driver"));

        btnLogin.setOnClickListener(v -> handleLogin());
        btnRegister.setOnClickListener(v -> handleRegister());

        btnGoogle.setOnClickListener(v ->
                Toast.makeText(this, "Google Login coming soon!", Toast.LENGTH_SHORT).show()
        );
    }

    private void setRoleSelection(String type) {
        selectedUserType = type;

        if (type.equals("passenger")) {
            // Highlight Passenger Card
            passengerCard.setStrokeColor(Color.parseColor("#3A86FF"));
            passengerCard.setStrokeWidth(6);
            passengerCard.setCardBackgroundColor(Color.parseColor("#E7F1FF"));

            // De-emphasize Driver Card
            driverCard.setStrokeColor(Color.parseColor("#E9ECEF"));
            driverCard.setStrokeWidth(2);
            driverCard.setCardBackgroundColor(Color.WHITE);
        } else {
            // Highlight Driver Card
            driverCard.setStrokeColor(Color.parseColor("#3A86FF"));
            driverCard.setStrokeWidth(6);
            driverCard.setCardBackgroundColor(Color.parseColor("#E7F1FF"));

            // De-emphasize Passenger Card
            passengerCard.setStrokeColor(Color.parseColor("#E9ECEF"));
            passengerCard.setStrokeWidth(2);
            passengerCard.setCardBackgroundColor(Color.WHITE);
        }
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        // Using safe query method from previous debug
        String query = "SELECT * FROM users WHERE email = ? AND user_type = ?";
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(query, new String[]{email, selectedUserType});

        if (cursor != null && cursor.moveToFirst()) {
            String userId = cursor.getString(cursor.getColumnIndexOrThrow("user_id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            cursor.close();

            // Proceed to home
            Intent intent = selectedUserType.equals("driver") ?
                    new Intent(this, DriverHomeActivity.class) :
                    new Intent(this, PassengerHomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "User not found. Please register.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleRegister() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Enter an email to register", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = UUID.randomUUID().toString();
        // Insert logic here...
        Toast.makeText(this, "Registered " + selectedUserType + "!", Toast.LENGTH_SHORT).show();
    }
}