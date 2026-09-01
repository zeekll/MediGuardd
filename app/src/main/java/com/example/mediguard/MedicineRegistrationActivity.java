package com.example.mediguard;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MedicineRegistrationActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private MaterialButton btnSaveMedicine;

    private AutoCompleteTextView actMedicineType;
    private AutoCompleteTextView actUnit;

    private TextInputEditText etMedicineName;
    private TextInputEditText etPurpose;
    private TextInputEditText etQuantity;
    private TextInputEditText etExpirationDate;

    private MaterialCardView cardPhoto;
    private ImageView imgPhoto;
    private TextView txtPhotoLabel;
    private TextView txtPhotoSubLabel;

    private DatabaseHelper databaseHelper;

    private boolean isEdit = false;
    private int medicineId = -1;

    // Photo state
    private String photoPath = null;
    private Uri pendingCameraUri = null;
    private File pendingCameraFile = null;

    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_registration);

        databaseHelper = new DatabaseHelper(this);

        // Restore pending camera file/photo path if the activity was
        // recreated (e.g. system killed it in the background) while the
        // camera app was open.
        if (savedInstanceState != null) {
            String restoredPendingPath = savedInstanceState.getString("pendingCameraFile");
            if (restoredPendingPath != null) {
                pendingCameraFile = new File(restoredPendingPath);
            }
            photoPath = savedInstanceState.getString("photoPath");
        }

        btnBack = findViewById(R.id.btnBack);
        btnSaveMedicine = findViewById(R.id.btnSaveMedicine);

        actMedicineType = findViewById(R.id.actMedicineType);
        actUnit = findViewById(R.id.actUnit);

        etMedicineName = findViewById(R.id.etMedicineName);
        etPurpose = findViewById(R.id.etPurpose);
        etQuantity = findViewById(R.id.etQuantity);
        etExpirationDate = findViewById(R.id.etExpirationDate);

        cardPhoto = findViewById(R.id.cardPhoto);
        imgPhoto = findViewById(R.id.imgPhoto);
        txtPhotoLabel = findViewById(R.id.txtPhotoLabel);
        txtPhotoSubLabel = findViewById(R.id.txtPhotoSubLabel);

        btnBack.setOnClickListener(v -> finish());

        // =========================
        // PHOTO LAUNCHERS
        // =========================

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && pendingCameraFile != null) {
                        photoPath = pendingCameraFile.getAbsolutePath();
                        showPhotoPreview();
                    } else {
                        Toast.makeText(this, "Photo capture cancelled", Toast.LENGTH_SHORT).show();
                    }
                });

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        String savedPath = copyImageToInternalStorage(uri);
                        if (savedPath != null) {
                            photoPath = savedPath;
                            showPhotoPreview();
                        } else {
                            Toast.makeText(this, "Could not load that image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        launchCamera();
                    } else {
                        Toast.makeText(this, "Camera permission is needed to take a photo", Toast.LENGTH_SHORT).show();
                    }
                });

        cardPhoto.setOnClickListener(v -> showPhotoSourceDialog());

        // =========================
        // TYPE DROPDOWN
        // =========================

        String[] medicineTypes = {
                "Tablet",
                "Capsule",
                "Syrup"
        };

        ArrayAdapter<String> typeAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        medicineTypes);

        actMedicineType.setAdapter(typeAdapter);

        // =========================
        // UNIT DROPDOWN
        // =========================

        String[] pieces = {"pcs"};
        String[] ml = {"mL"};

        ArrayAdapter<String> piecesAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        pieces);

        ArrayAdapter<String> mlAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        ml);

        actUnit.setAdapter(piecesAdapter);
        actUnit.setText("pcs", false);

        actMedicineType.setOnItemClickListener((parent, view, position, id) -> {

            if (actMedicineType.getText().toString().equals("Syrup")) {

                actUnit.setAdapter(mlAdapter);
                actUnit.setText("mL", false);

            } else {

                actUnit.setAdapter(piecesAdapter);
                actUnit.setText("pcs", false);

            }

        });

        // =========================
        // EDIT MODE
        // =========================

        isEdit = getIntent().getBooleanExtra("isEdit", false);

        if (isEdit) {

            medicineId = getIntent().getIntExtra("id", -1);

            btnSaveMedicine.setText("Update Medicine");

            etMedicineName.setText(getIntent().getStringExtra("name"));
            etPurpose.setText(getIntent().getStringExtra("purpose"));
            actMedicineType.setText(getIntent().getStringExtra("type"), false);
            etQuantity.setText(String.valueOf(
                    getIntent().getIntExtra("quantity", 0)));
            actUnit.setText(getIntent().getStringExtra("unit"), false);
            etExpirationDate.setText(getIntent().getStringExtra("expiry"));

            String existingPhoto = getIntent().getStringExtra("photo");

            if (existingPhoto != null && new File(existingPhoto).exists()) {
                photoPath = existingPhoto;
                showPhotoPreview();
            }

        }

        // If the activity was recreated while the camera was open, restore
        // the photo preview too.
        if (savedInstanceState != null && photoPath != null
                && new File(photoPath).exists()) {
            showPhotoPreview();
        }

        // =========================
        // EXPIRATION DATE PICKER
        // =========================

        etExpirationDate.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    MedicineRegistrationActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {

                        String date = (selectedMonth + 1) + "/"
                                + selectedDay + "/"
                                + selectedYear;

                        etExpirationDate.setText(date);

                    },
                    year,
                    month,
                    day
            );

            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

            dialog.show();

        });

        // =========================
        // SAVE / UPDATE BUTTON
        // =========================

        btnSaveMedicine.setOnClickListener(v -> {

            String medicine = etMedicineName.getText().toString().trim();
            String purpose = etPurpose.getText().toString().trim();
            String type = actMedicineType.getText().toString().trim();
            String quantity = etQuantity.getText().toString().trim();
            String unit = actUnit.getText().toString().trim();
            String expiration = etExpirationDate.getText().toString().trim();

            if (medicine.isEmpty()
                    || purpose.isEmpty()
                    || type.isEmpty()
                    || quantity.isEmpty()
                    || unit.isEmpty()
                    || expiration.isEmpty()
                    ) {

                Toast.makeText(
                        MedicineRegistrationActivity.this,
                        "Please complete all fields.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            boolean success;

            if (isEdit) {

                success = databaseHelper.updateMedicine(
                        getLoggedInUserId(),
                        medicineId,
                        medicine,
                        purpose,
                        type,
                        Integer.parseInt(quantity),
                        unit,
                        expiration,
                        photoPath
                );

                if (success) {

                    Toast.makeText(
                            MedicineRegistrationActivity.this,
                            "Medicine Updated",
                            Toast.LENGTH_SHORT
                    ).show();

                } else {

                    Toast.makeText(
                            MedicineRegistrationActivity.this,
                            "Update Failed",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

            } else {

                success = databaseHelper.insertMedicine(
                        getLoggedInUserId(),
                        medicine,
                        purpose,
                        type,
                        Integer.parseInt(quantity),
                        unit,
                        expiration,
                        photoPath
                );

                if (success) {

                    Toast.makeText(
                            MedicineRegistrationActivity.this,
                            "Medicine Saved",
                            Toast.LENGTH_SHORT
                    ).show();

                } else {

                    Toast.makeText(
                            MedicineRegistrationActivity.this,
                            "Failed to Save",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

            }

            finish();

        });

    }

    // =========================
    // PHOTO: SOURCE CHOOSER
    // =========================

    private void showPhotoSourceDialog() {

        String[] options = {"Take photo", "Choose from gallery"};

        new AlertDialog.Builder(this)
                .setTitle("Add medicine photo")
                .setItems(options, (dialog, which) -> {

                    if (which == 0) {
                        requestCameraAndLaunch();
                    } else {
                        pickImageLauncher.launch("image/*");
                    }

                })
                .show();
    }

    private void requestCameraAndLaunch() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {

        File photoFile = createImageFile();

        if (photoFile == null) {
            Toast.makeText(this, "Could not create photo file", Toast.LENGTH_SHORT).show();
            return;
        }

        pendingCameraFile = photoFile;

        pendingCameraUri = FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() + ".fileprovider",
                photoFile);

        takePictureLauncher.launch(pendingCameraUri);
    }

    private File createImageFile() {

        File storageDir = new File(getFilesDir(), "medicine_photos");

        if (!storageDir.exists() && !storageDir.mkdirs()) {
            return null;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new java.util.Date());

        return new File(storageDir, "MED_" + timeStamp + ".jpg");
    }

    // Copies a content:// image (e.g. from the gallery picker) into our own
    // private storage so the file stays readable even after the picker closes.
    private String copyImageToInternalStorage(Uri sourceUri) {

        File photoFile = createImageFile();

        if (photoFile == null) {
            return null;
        }

        try (InputStream in = getContentResolver().openInputStream(sourceUri);
             FileOutputStream out = new FileOutputStream(photoFile)) {

            if (in == null) {
                return null;
            }

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            return photoFile.getAbsolutePath();

        } catch (IOException e) {
            return null;
        }
    }

    private void showPhotoPreview() {

        if (photoPath == null) {
            return;
        }

        Bitmap bitmap = decodeSampledBitmap(photoPath, 300, 300);

        if (bitmap != null) {
            imgPhoto.setImageTintList(null);
            imgPhoto.setImageBitmap(bitmap);
            imgPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);

            android.view.ViewGroup.LayoutParams params = imgPhoto.getLayoutParams();
            params.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
            imgPhoto.setLayoutParams(params);

            // Hide both labels so only the photo fills the card
            txtPhotoLabel.setVisibility(View.GONE);
            txtPhotoSubLabel.setVisibility(View.GONE);

        } else {
            Toast.makeText(this, "Photo saved, but preview failed to load", Toast.LENGTH_SHORT).show();
        }
    }

    // Decodes a bitmap at a reduced size so large camera photos don't fail
    // to load (or eat huge amounts of memory) when shown as a small preview.
    private Bitmap decodeSampledBitmap(String path, int reqWidth, int reqHeight) {

        BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
        boundsOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, boundsOptions);

        int sampleSize = 1;
        int halfHeight = boundsOptions.outHeight / 2;
        int halfWidth = boundsOptions.outWidth / 2;

        while ((halfHeight / sampleSize) >= reqHeight
                && (halfWidth / sampleSize) >= reqWidth) {
            sampleSize *= 2;
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inSampleSize = sampleSize;

        return BitmapFactory.decodeFile(path, decodeOptions);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (pendingCameraFile != null) {
            outState.putString("pendingCameraFile", pendingCameraFile.getAbsolutePath());
        }

        if (photoPath != null) {
            outState.putString("photoPath", photoPath);
        }
    }
    private int getLoggedInUserId() {
        android.content.SharedPreferences preferences =
                getSharedPreferences(
                        "MediGuardPrefs",
                        MODE_PRIVATE
                );

        return preferences.getInt(
                "user_id",
                -1
        );
    }


}