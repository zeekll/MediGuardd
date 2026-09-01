package com.example.mediguard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.io.File;

public class SelectMedicineActivity extends AppCompatActivity {

    private ImageView imgMedicine;
    private TextView txtMedicineName;
    private TextView txtMedicineType;
    private TextView txtPurpose;
    private TextView txtType;
    private TextView txtQuantity;
    private TextView txtExpiry;

    private int medicineId;
    private int userId;
    private String photoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(
                R.layout.activity_select_medicine
        );

        ImageButton btnBack = findViewById(
                R.id.btnBack
        );

        imgMedicine = findViewById(
                R.id.imgMedicine
        );

        txtMedicineName = findViewById(
                R.id.txtMedicineName
        );

        txtMedicineType = findViewById(
                R.id.txtMedicineType
        );

        txtPurpose = findViewById(
                R.id.txtPurpose
        );

        txtType = findViewById(
                R.id.txtType
        );

        txtQuantity = findViewById(
                R.id.txtQuantity
        );

        txtExpiry = findViewById(
                R.id.txtExpiry
        );

        MaterialButton btnSetReminder =
                findViewById(
                        R.id.btnSetReminder
                );

        medicineId = getIntent().getIntExtra(
                "medicine_id",
                -1
        );

        userId = getIntent().getIntExtra(
                "user_id",
                -1
        );

        if (userId == -1) {
            SharedPreferences preferences =
                    getSharedPreferences(
                            "MediGuardPrefs",
                            MODE_PRIVATE
                    );

            userId = preferences.getInt(
                    "user_id",
                    -1
            );
        }

        String name = getIntent().getStringExtra(
                "medicine_name"
        );

        String purpose = getIntent().getStringExtra(
                "medicine_purpose"
        );

        String type = getIntent().getStringExtra(
                "medicine_type"
        );

        String quantity = getIntent().getStringExtra(
                "medicine_quantity"
        );

        String expiry = getIntent().getStringExtra(
                "medicine_expiry"
        );

        photoPath = getIntent().getStringExtra(
                "medicine_photo"
        );

        txtMedicineName.setText(name);
        txtMedicineType.setText(
                type + " • " + quantity
        );
        txtPurpose.setText(purpose);
        txtType.setText(type);
        txtQuantity.setText(quantity);
        txtExpiry.setText(expiry);

        loadPhoto();

        btnBack.setOnClickListener(
                v -> finish()
        );

        btnSetReminder.setOnClickListener(v -> {

            if (medicineId <= 0) {
                android.widget.Toast.makeText(
                        this,
                        "Medicine information is unavailable.",
                        android.widget.Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (userId <= 0) {
                android.widget.Toast.makeText(
                        this,
                        "User session not found. Please log in again.",
                        android.widget.Toast.LENGTH_SHORT
                ).show();

                return;
            }

            try {

                Intent intent = new Intent(
                        SelectMedicineActivity.this,
                        SetReminderActivity.class
                );

                intent.putExtra(
                        "user_id",
                        userId
                );

                intent.putExtra(
                        "medicine_id",
                        medicineId
                );

                intent.putExtra(
                        "medicine_name",
                        name
                );

                intent.putExtra(
                        "medicine_type",
                        type
                );

                intent.putExtra(
                        "medicine_quantity",
                        quantity
                );

                intent.putExtra(
                        "medicine_photo",
                        photoPath
                );

                startActivity(intent);

            } catch (Exception e) {

                android.util.Log.e(
                        "SelectMedicineActivity",
                        "Unable to open Set Reminder",
                        e
                );

                android.widget.Toast.makeText(
                        this,
                        "Unable to open Set Reminder.",
                        android.widget.Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void loadPhoto() {
        if (TextUtils.isEmpty(photoPath)) {
            return;
        }

        File file = new File(photoPath);

        if (!file.exists()) {
            return;
        }

        Bitmap bitmap = decodeSampledBitmap(
                photoPath,
                300,
                300
        );

        if (bitmap != null) {
            imgMedicine.setImageTintList(null);
            imgMedicine.setImageBitmap(bitmap);
        }
    }

    private Bitmap decodeSampledBitmap(
            String path,
            int reqWidth,
            int reqHeight
    ) {
        BitmapFactory.Options bounds =
                new BitmapFactory.Options();

        bounds.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(
                path,
                bounds
        );

        int sampleSize = 1;

        while (
                bounds.outWidth / sampleSize > reqWidth * 2
                        &&
                        bounds.outHeight / sampleSize > reqHeight * 2
        ) {
            sampleSize *= 2;
        }

        BitmapFactory.Options options =
                new BitmapFactory.Options();

        options.inSampleSize = sampleSize;

        return BitmapFactory.decodeFile(
                path,
                options
        );
    }
}
