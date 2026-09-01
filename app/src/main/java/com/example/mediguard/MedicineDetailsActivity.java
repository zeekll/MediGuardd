package com.example.mediguard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.File;

public class MedicineDetailsActivity extends AppCompatActivity {

    private ImageButton btnBack;

    private TextView txtName;
    private TextView txtPurpose;
    private TextView txtType;
    private TextView txtQuantity;
    private TextView txtExpiry;

    private MaterialButton btnEdit;
    private MaterialButton btnDelete;

    private ImageView imgPillIcon;
    private MaterialCardView cardPhotoPreview;
    private ImageView imgMedicinePhoto;

    private DatabaseHelper databaseHelper;

    private int medicineId;
    private int userId;
    private String medicinePhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_details);

        // ==========================
        // Initialize Views
        // ==========================

        btnBack = findViewById(R.id.btnBack);

        txtName = findViewById(R.id.txtName);
        txtPurpose = findViewById(R.id.txtPurpose);
        txtType = findViewById(R.id.txtType);
        txtQuantity = findViewById(R.id.txtQuantity);
        txtExpiry = findViewById(R.id.txtExpiry);

        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        imgPillIcon = findViewById(R.id.imgPillIcon);
        cardPhotoPreview = findViewById(R.id.cardPhotoPreview);
        imgMedicinePhoto = findViewById(R.id.imgMedicinePhoto);

        databaseHelper = new DatabaseHelper(this);

        // ==========================
        // Get Medicine Data
        // ==========================

        medicineId = getIntent().getIntExtra("id", -1);

        userId = getSharedPreferences(
                "MediGuardPrefs",
                MODE_PRIVATE
        ).getInt(
                "user_id",
                -1
        );

        txtName.setText(
                getIntent().getStringExtra("name")
        );

        txtPurpose.setText(
                getIntent().getStringExtra("purpose")
        );

        txtType.setText(
                getIntent().getStringExtra("type")
        );

        txtQuantity.setText(
                getIntent().getIntExtra("quantity", 0)
                        + " "
                        + getIntent().getStringExtra("unit")
        );

        txtExpiry.setText(
                getIntent().getStringExtra("expiry")
        );

        medicinePhotoPath =
                getIntent().getStringExtra("photo");

        // ==========================
        // Load Medicine Photo
        // ==========================

        if (medicinePhotoPath != null
                && new File(medicinePhotoPath).exists()) {

            Bitmap bitmap = decodeSampledBitmap(
                    medicinePhotoPath,
                    400,
                    400
            );

            if (bitmap != null) {

                imgMedicinePhoto.setImageTintList(null);
                imgMedicinePhoto.setImageBitmap(bitmap);

                cardPhotoPreview.setVisibility(
                        View.VISIBLE
                );

                imgPillIcon.setVisibility(
                        View.GONE
                );
            }
        }

        // ==========================
        // Back Button
        // ==========================

        btnBack.setOnClickListener(v -> finish());

        // ==========================
        // Photo Click
        // ==========================

        imgMedicinePhoto.setOnClickListener(
                v -> showFullImage()
        );

        cardPhotoPreview.setOnClickListener(
                v -> showFullImage()
        );

        // ==========================
        // Delete Button
        // ==========================

        btnDelete.setOnClickListener(v -> {

            new AlertDialog.Builder(
                    MedicineDetailsActivity.this
            )
                    .setTitle("Delete Medicine")
                    .setMessage(
                            "Are you sure you want to delete this medicine?"
                    )
                    .setPositiveButton(
                            "Delete",
                            (dialog, which) -> {

                                boolean deleted =
                                        databaseHelper.deleteMedicine(
                                                userId,
                                                medicineId
                                        );

                                if (deleted) {

                                    Toast.makeText(
                                            MedicineDetailsActivity.this,
                                            "Medicine Deleted",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    finish();

                                } else {

                                    Toast.makeText(
                                            MedicineDetailsActivity.this,
                                            "Delete Failed",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }
                    )
                    .setNegativeButton(
                            "Cancel",
                            null
                    )
                    .show();
        });

        // ==========================
        // Edit Button
        // ==========================

        btnEdit.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MedicineDetailsActivity.this,
                    MedicineRegistrationActivity.class
            );

            intent.putExtra("isEdit", true);
            intent.putExtra("id", medicineId);

            intent.putExtra(
                    "name",
                    txtName.getText().toString()
            );

            intent.putExtra(
                    "purpose",
                    txtPurpose.getText().toString()
            );

            intent.putExtra(
                    "type",
                    txtType.getText().toString()
            );

            String quantityText =
                    txtQuantity.getText().toString();

            String[] parts =
                    quantityText.split(" ");

            int quantity = 0;
            String unit = "";

            if (parts.length >= 2) {

                try {
                    quantity = Integer.parseInt(parts[0]);
                    unit = parts[1];
                } catch (NumberFormatException e) {
                    quantity = 0;
                    unit = "";
                }
            }

            intent.putExtra("quantity", quantity);
            intent.putExtra("unit", unit);

            intent.putExtra(
                    "expiry",
                    txtExpiry.getText().toString()
            );

            intent.putExtra(
                    "photo",
                    medicinePhotoPath
            );

            startActivity(intent);
            finish();
        });
    }

    // ==========================
    // SHOW LARGE ZOOMABLE PHOTO
    // ==========================

    private void showFullImage() {

        if (medicinePhotoPath == null
                || !new File(medicinePhotoPath).exists()) {

            Toast.makeText(
                    this,
                    "Image not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Bitmap bitmap = decodeSampledBitmap(
                medicinePhotoPath,
                1600,
                1600
        );

        if (bitmap == null) {

            Toast.makeText(
                    this,
                    "Unable to load image",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        final Dialog dialog = new Dialog(this);

        ZoomableImageView zoomImage =
                new ZoomableImageView(this);

        zoomImage.setImageBitmap(bitmap);
        zoomImage.setScaleType(
                ImageView.ScaleType.FIT_CENTER
        );

        int screenWidth =
                getResources()
                        .getDisplayMetrics()
                        .widthPixels;

        int screenHeight =
                getResources()
                        .getDisplayMetrics()
                        .heightPixels;

        int imageWidth =
                (int) (screenWidth * 0.95);

        int imageHeight =
                (int) (screenHeight * 0.85);

        zoomImage.setLayoutParams(
                new ViewGroup.LayoutParams(
                        imageWidth,
                        imageHeight
                )
        );

        dialog.setContentView(zoomImage);

        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        if (dialog.getWindow() != null) {

            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(
                            Color.TRANSPARENT
                    )
            );
        }

        dialog.show();

        if (dialog.getWindow() != null) {

            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(
                            Color.TRANSPARENT
                    )
            );

            dialog.getWindow().setLayout(
                    imageWidth,
                    imageHeight
            );
        }
    }

    // ==========================
    // ZOOMABLE IMAGE VIEW
    // ==========================

    public static class ZoomableImageView extends ImageView {

        private Matrix matrix = new Matrix();

        private float scale = 1.0f;

        private float minScale = 1.0f;
        private float maxScale = 4.0f;

        private float lastX;
        private float lastY;

        private boolean isDragging = false;

        private ScaleGestureDetector scaleDetector;

        public ZoomableImageView(
                android.content.Context context) {

            super(context);

            setScaleType(
                    ImageView.ScaleType.MATRIX
            );

            scaleDetector =
                    new ScaleGestureDetector(
                            context,
                            new ScaleListener()
                    );
        }

        @Override
        public boolean onTouchEvent(
                MotionEvent event) {

            scaleDetector.onTouchEvent(event);

            switch (event.getActionMasked()) {

                case MotionEvent.ACTION_DOWN:

                    lastX = event.getX();
                    lastY = event.getY();

                    isDragging = true;

                    return true;

                case MotionEvent.ACTION_MOVE:

                    if (!scaleDetector.isInProgress()
                            && isDragging
                            && scale > minScale) {

                        float x =
                                event.getX();

                        float y =
                                event.getY();

                        float dx =
                                x - lastX;

                        float dy =
                                y - lastY;

                        matrix.postTranslate(
                                dx,
                                dy
                        );

                        setImageMatrix(matrix);

                        lastX = x;
                        lastY = y;
                    }

                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:

                    isDragging = false;

                    return true;
            }

            return true;
        }

        private class ScaleListener
                extends ScaleGestureDetector.SimpleOnScaleGestureListener {

            @Override
            public boolean onScale(
                    ScaleGestureDetector detector) {

                float scaleFactor =
                        detector.getScaleFactor();

                float newScale =
                        scale * scaleFactor;

                if (newScale < minScale) {
                    scaleFactor =
                            minScale / scale;
                }

                if (newScale > maxScale) {
                    scaleFactor =
                            maxScale / scale;
                }

                matrix.postScale(
                        scaleFactor,
                        scaleFactor,
                        detector.getFocusX(),
                        detector.getFocusY()
                );

                scale *= scaleFactor;

                setImageMatrix(matrix);

                return true;
            }
        }
    }

    // ==========================
    // DECODE PHOTO
    // ==========================

    private Bitmap decodeSampledBitmap(
            String path,
            int reqWidth,
            int reqHeight) {

        BitmapFactory.Options boundsOptions =
                new BitmapFactory.Options();

        boundsOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(
                path,
                boundsOptions
        );

        int sampleSize = 1;

        int halfHeight =
                boundsOptions.outHeight / 2;

        int halfWidth =
                boundsOptions.outWidth / 2;

        while (
                (halfHeight / sampleSize) >= reqHeight
                        &&
                        (halfWidth / sampleSize) >= reqWidth
        ) {

            sampleSize *= 2;
        }

        BitmapFactory.Options decodeOptions =
                new BitmapFactory.Options();

        decodeOptions.inSampleSize =
                sampleSize;

        return BitmapFactory.decodeFile(
                path,
                decodeOptions
        );
    }
}