package com.example.mediguard;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Tap-through screen for one medicine's saved reminder. Covers:
 *  - Step 2: view today's schedule & details
 *  - Step 4: Mark as Taken (manual, in-app)
 *  - Step 5: Missed dose banner (auto MISSED after the time window)
 *  - Step 6: Reminder ON/OFF (no alerts, no SMS while OFF)
 */
public class ReminderDetailsActivity extends AppCompatActivity {

    private static final SimpleDateFormat SCHEDULED_AT_FORMAT =
            new SimpleDateFormat(ReminderScheduler.SCHEDULED_AT_FORMAT, Locale.US);

    private static final SimpleDateFormat TIME_LABEL_FORMAT =
            new SimpleDateFormat("h:mm a", Locale.US);

    private static final SimpleDateFormat TAKEN_AT_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    private DatabaseHelper databaseHelper;

    private int userId;
    private int medicineId;
    private String medicineName;
    private String medicineType;
    private String medicineQuantity;
    private String medicinePhoto;

    private TextView txtHeaderTitle;
    private TextView txtMedicineName;
    private TextView txtMedicineType;
    private TextView txtDose;
    private ImageView imgMedicine;

    private MaterialCardView cardPending;
    private TextView txtPendingDetails;
    private MaterialButton btnMarkAsTaken;
    private MaterialButton btnSnooze;

    private MaterialCardView cardMissed;
    private TextView txtMissedDetails;

    private TextView txtStartDate;
    private TextView txtRepeatEvery;
    private TextView txtDuration;
    private TextView txtContactNumber;
    private SwitchMaterial switchReminderOnOff;
    private MaterialCardView cardReminderOff;

    private RecyclerView recyclerSchedule;
    private TextView txtNoSchedule;

    private MaterialButton btnEditReminder;
    private MaterialButton btnDeleteReminder;

    private String selectedPendingScheduledAt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_details);

        databaseHelper = new DatabaseHelper(this);

        userId = getIntent().getIntExtra("user_id", -1);
        medicineId = getIntent().getIntExtra("medicine_id", -1);
        medicineName = getIntent().getStringExtra("medicine_name");
        medicineType = getIntent().getStringExtra("medicine_type");
        medicineQuantity = getIntent().getStringExtra("medicine_quantity");
        medicinePhoto = getIntent().getStringExtra("medicine_photo");

        if (userId == -1) {
            userId = getSharedPreferences("MediGuardPrefs", MODE_PRIVATE)
                    .getInt("user_id", -1);
        }

        bindViews();

        txtHeaderTitle.setText(
                TextUtils.isEmpty(medicineName) ? "Reminder" : medicineName
        );

        txtMedicineName.setText(medicineName);
        txtMedicineType.setText(
                TextUtils.isEmpty(medicineQuantity)
                        ? medicineType
                        : medicineType + " • " + medicineQuantity
        );

        loadPhoto();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnMarkAsTaken.setOnClickListener(v -> markAsTaken());
        btnSnooze.setOnClickListener(v -> snoozeReminder());

        switchReminderOnOff.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (buttonView.isPressed()) {
                        onReminderToggled(isChecked);
                    }
                }
        );

        btnEditReminder.setOnClickListener(v -> openEditReminder());
        btnDeleteReminder.setOnClickListener(v -> confirmDeleteReminder());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void bindViews() {

        txtHeaderTitle = findViewById(R.id.txtHeaderTitle);

        imgMedicine = findViewById(R.id.imgMedicine);
        txtMedicineName = findViewById(R.id.txtMedicineName);
        txtMedicineType = findViewById(R.id.txtMedicineType);
        txtDose = findViewById(R.id.txtDose);

        cardPending = findViewById(R.id.cardPending);
        txtPendingDetails = findViewById(R.id.txtPendingDetails);
        btnMarkAsTaken = findViewById(R.id.btnMarkAsTaken);
        btnSnooze = findViewById(R.id.btnSnooze);

        cardMissed = findViewById(R.id.cardMissed);
        txtMissedDetails = findViewById(R.id.txtMissedDetails);

        txtStartDate = findViewById(R.id.txtStartDate);
        txtRepeatEvery = findViewById(R.id.txtRepeatEvery);
        txtDuration = findViewById(R.id.txtDuration);
        txtContactNumber = findViewById(R.id.txtContactNumber);
        switchReminderOnOff = findViewById(R.id.switchReminderOnOff);
        cardReminderOff = findViewById(R.id.cardReminderOff);

        recyclerSchedule = findViewById(R.id.recyclerSchedule);
        txtNoSchedule = findViewById(R.id.txtNoSchedule);

        recyclerSchedule.setLayoutManager(new LinearLayoutManager(this));

        btnEditReminder = findViewById(R.id.btnEditReminder);
        btnDeleteReminder = findViewById(R.id.btnDeleteReminder);
    }

    // =====================================================
    // REFRESH (reminder info + today's schedule)
    // =====================================================

    private void refresh() {

        if (userId == -1 || medicineId == -1) {
            Toast.makeText(this, "Reminder not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ReminderConfig config = databaseHelper.getReminderConfig(userId, medicineId);

        if (config == null) {
            Toast.makeText(this, "This medicine has no saved reminder.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        txtDose.setText("Dose: " + config.getDose() + " " + config.getDoseUnit());
        txtStartDate.setText(config.getStartDate());
        txtRepeatEvery.setText("Every " + config.getRepeatHours() + " hours");
        txtDuration.setText(config.getDurationDays() + " days");
        txtContactNumber.setText(config.getContactNumber());

        switchReminderOnOff.setOnCheckedChangeListener(null);
        switchReminderOnOff.setChecked(config.isEnabled());
        switchReminderOnOff.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (buttonView.isPressed()) {
                        onReminderToggled(isChecked);
                    }
                }
        );

        cardReminderOff.setVisibility(
                config.isEnabled() ? View.GONE : View.VISIBLE
        );

        loadTodaySchedule(config);
    }

    private void loadTodaySchedule(ReminderConfig config) {

        Calendar today = Calendar.getInstance();

        List<Calendar> occurrences =
                ReminderScheduleCalculator.getDoseTimesForDate(config, today);

        Collections.sort(
                occurrences,
                Comparator.comparingLong(Calendar::getTimeInMillis)
        );

        List<ScheduleDoseAdapter.DoseRow> rows = new ArrayList<>();

        String amountLabel = config.getDose() + " " + config.getDoseUnit();

        selectedPendingScheduledAt = null;
        String pendingTimeLabel = null;
        String missedTimeLabel = null;

        for (Calendar occurrence : occurrences) {

            String scheduledAt = SCHEDULED_AT_FORMAT.format(occurrence.getTime());
            String timeLabel = TIME_LABEL_FORMAT.format(occurrence.getTime());

            String status = "UPCOMING";

            Cursor cursor = databaseHelper.getDoseLog(userId, medicineId, scheduledAt);

            if (cursor.moveToFirst()) {
                status = cursor.getString(
                        cursor.getColumnIndexOrThrow("status")
                );
            }

            cursor.close();

            rows.add(new ScheduleDoseAdapter.DoseRow(
                    scheduledAt, timeLabel, amountLabel, status
            ));

            if (DatabaseHelper.DOSE_STATUS_PENDING.equals(status)
                    && selectedPendingScheduledAt == null) {

                selectedPendingScheduledAt = scheduledAt;
                pendingTimeLabel = timeLabel;
            }

            if (DatabaseHelper.DOSE_STATUS_MISSED.equals(status)) {
                missedTimeLabel = timeLabel;
            }
        }

        recyclerSchedule.setAdapter(new ScheduleDoseAdapter(this, rows));

        boolean empty = rows.isEmpty();
        recyclerSchedule.setVisibility(empty ? View.GONE : View.VISIBLE);
        txtNoSchedule.setVisibility(empty ? View.VISIBLE : View.GONE);

        // Step 4: "Time to take your medicine" card
        if (selectedPendingScheduledAt != null && config.isEnabled()) {

            cardPending.setVisibility(View.VISIBLE);

            txtPendingDetails.setText(
                    "Dose: " + amountLabel + "\nScheduled: " + pendingTimeLabel
            );

        } else {
            cardPending.setVisibility(View.GONE);
        }

        // Step 5: Missed dose banner
        if (missedTimeLabel != null) {

            cardMissed.setVisibility(View.VISIBLE);

            txtMissedDetails.setText(
                    "The " + missedTimeLabel + " dose wasn't confirmed within the "
                            + ReminderScheduler.MISS_WINDOW_MINUTES
                            + "-minute window, so it was automatically marked MISSED."
            );

        } else {
            cardMissed.setVisibility(View.GONE);
        }
    }

    // =====================================================
    // STEP 4: MARK AS TAKEN / SNOOZE
    // =====================================================

    private void markAsTaken() {

        if (selectedPendingScheduledAt == null) {
            return;
        }

        String takenAt = TAKEN_AT_FORMAT.format(Calendar.getInstance().getTime());

        boolean updated = databaseHelper.markDoseTaken(
                userId, medicineId, selectedPendingScheduledAt, takenAt
        );

        if (updated) {

            ReminderScheduler.cancelMissCheck(this, medicineId);

            Toast.makeText(this, "Marked as Taken", Toast.LENGTH_SHORT).show();

            refresh();

        } else {

            Toast.makeText(
                    this,
                    "This dose was already resolved.",
                    Toast.LENGTH_SHORT
            ).show();

            refresh();
        }
    }

    private void snoozeReminder() {

        if (selectedPendingScheduledAt == null) {
            return;
        }

        ReminderScheduler.snoozeMissCheck(
                this, userId, medicineId, selectedPendingScheduledAt, 15
        );

        Toast.makeText(
                this,
                "Snoozed for 15 minutes",
                Toast.LENGTH_SHORT
        ).show();
    }

    // =====================================================
    // STEP 6: REMINDER ON/OFF
    // =====================================================

    private void onReminderToggled(boolean enabled) {

        databaseHelper.setReminderEnabled(userId, medicineId, enabled);

        if (enabled) {

            ReminderScheduler.scheduleNext(this, userId, medicineId);

            Toast.makeText(this, "Reminder turned ON", Toast.LENGTH_SHORT).show();

        } else {

            ReminderScheduler.cancel(this, medicineId);
            ReminderScheduler.cancelMissCheck(this, medicineId);

            Toast.makeText(
                    this,
                    "Reminder turned OFF. No alerts or SMS will be sent.",
                    Toast.LENGTH_SHORT
            ).show();
        }

        cardReminderOff.setVisibility(enabled ? View.GONE : View.VISIBLE);

        refresh();
    }

    // =====================================================
    // EDIT / DELETE
    // =====================================================

    private void openEditReminder() {

        Intent intent = new Intent(this, SetReminderActivity.class);

        intent.putExtra("user_id", userId);
        intent.putExtra("medicine_id", medicineId);
        intent.putExtra("medicine_name", medicineName);
        intent.putExtra("medicine_type", medicineType);
        intent.putExtra("medicine_quantity", medicineQuantity);
        intent.putExtra("medicine_photo", medicinePhoto);

        startActivity(intent);
    }

    private void confirmDeleteReminder() {

        new AlertDialog.Builder(this)
                .setTitle("Delete Reminder")
                .setMessage("Are you sure you want to delete this reminder? This won't delete the medicine itself.")
                .setPositiveButton("Delete", (dialog, which) -> {

                    databaseHelper.deleteReminder(userId, medicineId);

                    ReminderScheduler.cancel(this, medicineId);
                    ReminderScheduler.cancelMissCheck(this, medicineId);

                    Toast.makeText(this, "Reminder Deleted", Toast.LENGTH_SHORT).show();

                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // =====================================================
    // PHOTO
    // =====================================================

    private void loadPhoto() {

        if (TextUtils.isEmpty(medicinePhoto)) {
            return;
        }

        File file = new File(medicinePhoto);

        if (!file.exists()) {
            return;
        }

        Bitmap bitmap = decodeSampledBitmap(medicinePhoto, 200, 200);

        if (bitmap != null) {
            imgMedicine.setImageTintList(null);
            imgMedicine.setImageBitmap(bitmap);
        }
    }

    private Bitmap decodeSampledBitmap(String path, int reqWidth, int reqHeight) {

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bounds);

        int sampleSize = 1;

        while (
                bounds.outWidth / sampleSize > reqWidth * 2
                        && bounds.outHeight / sampleSize > reqHeight * 2
        ) {
            sampleSize *= 2;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;

        return BitmapFactory.decodeFile(path, options);
    }
}