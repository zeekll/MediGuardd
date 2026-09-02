package com.example.mediguard;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Reached from the Home "Medication Reminder" card. Lists every
 * medicine (whether or not it already has a reminder) so the person
 * can pick one and set a reminder for it.
 *
 * This is intentionally separate from MedicationReminderActivity,
 * which is the Alerts tab's "already-saved reminders" list -- keeping
 * them apart means changes to one don't affect the other.
 */
public class ChooseMedicineReminderActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private ArrayList<Medicine> medicineList;
    private MedicineChoiceAdapter adapter;

    private RecyclerView recyclerMedicines;
    private TextView txtEmpty;

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_choose_medicine_reminder);

        databaseHelper = new DatabaseHelper(this);

        userId = getLoggedInUserId();

        ImageButton btnBack = findViewById(R.id.btnBack);

        recyclerMedicines = findViewById(R.id.recyclerMedicines);
        txtEmpty = findViewById(R.id.txtEmpty);

        btnBack.setOnClickListener(v -> finish());

        medicineList = new ArrayList<>();

        recyclerMedicines.setLayoutManager(new LinearLayoutManager(this));

        loadMedicines();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMedicines();
    }

    private int getLoggedInUserId() {

        SharedPreferences preferences =
                getSharedPreferences("MediGuardPrefs", MODE_PRIVATE);

        return preferences.getInt("user_id", -1);
    }

    private void loadMedicines() {

        medicineList.clear();

        if (userId == -1) {
            showEmptyState();
            return;
        }

        Cursor cursor = null;

        try {

            cursor = databaseHelper.getAllMedicines(userId);

            while (cursor.moveToNext()) {

                Medicine medicine = new Medicine(
                        cursor.getInt(cursor.getColumnIndexOrThrow("medicine_id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("medicine_name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("purpose")),
                        cursor.getString(cursor.getColumnIndexOrThrow("type")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                        cursor.getString(cursor.getColumnIndexOrThrow("unit")),
                        cursor.getString(cursor.getColumnIndexOrThrow("expiry_date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("photo_path"))
                );

                medicineList.add(medicine);
            }

        } catch (Exception e) {

            android.util.Log.e(
                    "ChooseMedicineReminder",
                    "Unable to load medicines",
                    e
            );

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        adapter = new MedicineChoiceAdapter(this, medicineList);
        recyclerMedicines.setAdapter(adapter);

        if (medicineList.isEmpty()) {
            showEmptyState();
        } else {
            txtEmpty.setVisibility(View.GONE);
            recyclerMedicines.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyState() {
        txtEmpty.setVisibility(View.VISIBLE);
        recyclerMedicines.setVisibility(View.GONE);
    }
}