package com.example.mediguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Fired MISS_CHECK_WINDOW_MILLIS after a dose was triggered.
 *
 * Step 5: if that dose is still PENDING (the person never tapped
 * "Mark as Taken"), flip it to MISSED and let them know. If it was
 * already marked TAKEN, markDoseMissed() is a no-op and nothing
 * else happens here.
 */
public class DoseMissCheckReceiver extends BroadcastReceiver {

    private static final String TAG = "DoseMissCheck";

    @Override
    public void onReceive(Context context, Intent intent) {

        int userId = intent.getIntExtra(
                ReminderScheduler.EXTRA_USER_ID, -1
        );

        int medicineId = intent.getIntExtra(
                ReminderScheduler.EXTRA_MEDICINE_ID, -1
        );

        String scheduledAt = intent.getStringExtra(
                ReminderScheduler.EXTRA_SCHEDULED_AT
        );

        if (userId == -1 || medicineId == -1 || scheduledAt == null) {
            Log.e(TAG, "Missing extras, cannot run miss-check.");
            return;
        }

        DatabaseHelper databaseHelper = new DatabaseHelper(context);

        boolean flippedToMissed =
                databaseHelper.markDoseMissed(userId, medicineId, scheduledAt);

        if (!flippedToMissed) {
            // Already TAKEN (or already MISSED) before the deadline.
            return;
        }

        Log.i(
                TAG,
                "Dose missed: user=" + userId
                        + " medicine=" + medicineId
                        + " scheduledAt=" + scheduledAt
        );

        String medicineName = databaseHelper.getMedicineName(medicineId);

        ReminderNotifier.showMissedDoseNotification(
                context,
                medicineId,
                medicineName != null ? medicineName : "your medicine",
                scheduledAt
        );
    }
}