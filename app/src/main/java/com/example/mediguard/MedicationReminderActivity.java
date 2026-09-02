package com.example.mediguard;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MedicationReminderActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private ArrayList<Medicine> medicineList;
    private MedicationReminderAdapter adapter;

    private RecyclerView recyclerMedicines;
    private TextView txtEmpty;
    private TextView txtOverview;

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_medication_reminder
        );

        databaseHelper = new DatabaseHelper(this);

        userId = getLoggedInUserId();

        ImageButton btnBack = findViewById(
                R.id.btnBack
        );

        recyclerMedicines = findViewById(
                R.id.recyclerMedicines
        );

        txtEmpty = findViewById(
                R.id.txtEmpty
        );

        txtOverview = findViewById(
                R.id.txtOverview
        );

        btnBack.setOnClickListener(
                v -> finish()
        );

        medicineList = new ArrayList<>();

        recyclerMedicines.setLayoutManager(
                new LinearLayoutManager(this)
        );

        loadMedicines();
    }

    // =====================================================
    // GET LOGGED-IN USER
    // =====================================================

    private int getLoggedInUserId() {

        SharedPreferences preferences =
                getSharedPreferences(
                        "MediGuardPrefs",
                        MODE_PRIVATE
                );

        return preferences.getInt(
                "user_id",
                -1
        );
    }

    // =====================================================
    // LOAD MEDICINES
    // =====================================================

    private void loadMedicines() {

        medicineList.clear();

        if (userId == -1) {
            showEmptyState();
            return;
        }

        Cursor cursor = null;

        try {

            cursor = databaseHelper.getMedicinesWithReminders(
                    userId
            );

            while (cursor.moveToNext()) {

                Medicine medicine = new Medicine(
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow(
                                        "medicine_id"
                                )
                        ),

                        cursor.getInt(
                                cursor.getColumnIndexOrThrow(
                                        "user_id"
                                )
                        ),

                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        "medicine_name"
                                )
                        ),

                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        "purpose"
                                )
                        ),

                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        "type"
                                )
                        ),

                        cursor.getInt(
                                cursor.getColumnIndexOrThrow(
                                        "quantity"
                                )
                        ),

                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        "unit"
                                )
                        ),

                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        "expiry_date"
                                )
                        ),

                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        "photo_path"
                                )
                        )
                );

                medicineList.add(
                        medicine
                );
            }

        } catch (Exception e) {

            android.util.Log.e(
                    "MedicationReminder",
                    "Unable to load medicines",
                    e
            );

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        adapter = new MedicationReminderAdapter(
                this,
                medicineList
        );

        recyclerMedicines.setAdapter(
                adapter
        );

        int count = medicineList.size();

        txtOverview.setText(
                "You have " + count
                        + (count == 1 ? " saved reminder.\n" : " saved reminders.\n")
                        + "Tap a reminder to view its schedule."
        );

        if (medicineList.isEmpty()) {

            showEmptyState();

        } else {

            txtEmpty.setVisibility(
                    android.view.View.GONE
            );

            recyclerMedicines.setVisibility(
                    android.view.View.VISIBLE
            );
        }
    }

    // =====================================================
    // EMPTY STATE
    // =====================================================

    private void showEmptyState() {

        txtEmpty.setVisibility(
                android.view.View.VISIBLE
        );

        recyclerMedicines.setVisibility(
                android.view.View.GONE
        );
    }

    // =====================================================
    // REFRESH MEDICINES
    // =====================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (databaseHelper != null) {

            loadMedicines();
        }
    }
}