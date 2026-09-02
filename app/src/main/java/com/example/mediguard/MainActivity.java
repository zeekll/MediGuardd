package com.example.mediguard;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_REMINDER_PERMISSIONS = 1001;

    private LinearLayout cardMedicine;
    private LinearLayout cardReminder;
    private LinearLayout cardExpiry;
    private LinearLayout cardInquiry;

    private TextView txtHello;

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestReminderPermissionsIfNeeded();

        // ==============================
        // Initialize Views
        // ==============================

        txtHello = findViewById(R.id.txtHello);

        cardMedicine = findViewById(R.id.cardMedicine);
        cardReminder = findViewById(R.id.cardReminder);
        cardExpiry = findViewById(R.id.cardExpiry);
        cardInquiry = findViewById(R.id.cardInquiry);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        // ==============================
        // GET LOGGED-IN USERNAME
        // ==============================

        String username =
                getIntent().getStringExtra("username");

        int userId =
                getIntent().getIntExtra(
                        "user_id",
                        -1
                );

        SharedPreferences preferences =
                getSharedPreferences(
                        "MediGuardPrefs",
                        MODE_PRIVATE
                );

        if (userId != -1) {
            preferences.edit()
                    .putInt(
                            "user_id",
                            userId
                    )
                    .apply();
        }

        // If username came from Login
        if (username != null && !username.isEmpty()) {

            // Save username so it remains available
            // when returning to Dashboard
            preferences.edit()
                    .putString("username", username)
                    .apply();

        } else {

            // Get saved username
            username =
                    preferences.getString(
                            "username",
                            "User"
                    );
        }

        // ==============================
        // DISPLAY USERNAME
        // ==============================

        txtHello.setText(
                "Hello, " + username + "! 👋"
        );

        // ==============================
        // DASHBOARD CARDS
        // ==============================

        cardMedicine.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    MedicineRegistrationActivity.class
            );

            startActivity(intent);
        });

        cardReminder.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    MedicationReminderActivity.class
            );

            startActivity(intent);
        });

        cardExpiry.setOnClickListener(v -> {

            Toast.makeText(
                    MainActivity.this,
                    "Expiry & Quantity\nComing Soon",
                    Toast.LENGTH_SHORT
            ).show();
        });

        cardInquiry.setOnClickListener(v -> {

            Toast.makeText(
                    MainActivity.this,
                    "Medicine Inquiry\nComing Soon",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // ==============================
        // BOTTOM NAVIGATION
        // ==============================

        bottomNavigation.setSelectedItemId(
                R.id.nav_home
        );

        bottomNavigation.setOnItemSelectedListener(
                item -> {

                    int id = item.getItemId();

                    // HOME
                    if (id == R.id.nav_home) {
                        return true;
                    }

                    // MEDICINES
                    if (id == R.id.nav_medicine) {

                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        MedicineListActivity.class
                                )
                        );

                        return true;
                    }

                    // ALERTS
                    if (id == R.id.nav_alerts) {

                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        MedicationReminderActivity.class
                                )
                        );

                        return true;
                    }

                    // PROFILE
                    if (id == R.id.nav_profile) {

                        Toast.makeText(
                                MainActivity.this,
                                "Profile Coming Soon",
                                Toast.LENGTH_SHORT
                        ).show();

                        return true;
                    }

                    return false;
                }
        );
    }

    // ==============================
    // REFRESH USERNAME
    // ==============================

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences preferences =
                getSharedPreferences(
                        "MediGuardPrefs",
                        MODE_PRIVATE
                );

        String username =
                preferences.getString(
                        "username",
                        "User"
                );

        txtHello.setText(
                "Hello, " + username + "! 👋"
        );
    }

    // ==============================
    // STEP 3 PERMISSIONS (SMS + notifications)
    // ==============================

    private void requestReminderPermissionsIfNeeded() {

        List<String> toRequest = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.SEND_SMS
        ) != PackageManager.PERMISSION_GRANTED) {
            toRequest.add(Manifest.permission.SEND_SMS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {
            toRequest.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (!toRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    toRequest.toArray(new String[0]),
                    REQUEST_CODE_REMINDER_PERMISSIONS
            );
        }
    }
}