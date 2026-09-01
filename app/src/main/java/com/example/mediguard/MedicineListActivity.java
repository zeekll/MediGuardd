package com.example.mediguard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MedicineListActivity extends AppCompatActivity {

    private RecyclerView recyclerMedicines;
    private DatabaseHelper databaseHelper;
    private ArrayList<Medicine> medicineList;
    private MedicineAdapter medicineAdapter;

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_list);

        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MedicineListActivity.this,
                    MainActivity.class
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            );

            startActivity(intent);
            finish();
        });

        recyclerMedicines = findViewById(
                R.id.recyclerMedicines
        );

        databaseHelper = new DatabaseHelper(this);
        medicineList = new ArrayList<>();

        userId = getLoggedInUserId();

        recyclerMedicines.setLayoutManager(
                new LinearLayoutManager(this)
        );

        loadMedicines();
    }

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

    private void loadMedicines() {
        medicineList.clear();

        if (userId == -1) {
            return;
        }

        Cursor cursor =
                databaseHelper.getAllMedicines(userId);

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

            medicineList.add(medicine);
        }

        cursor.close();

        medicineAdapter = new MedicineAdapter(
                this,
                medicineList
        );

        recyclerMedicines.setAdapter(
                medicineAdapter
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMedicines();
    }
}
