package com.example.mediguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Fired MISS_WINDOW_MINUTES after a dose was triggered (step 3). If
 * the dose is still PENDING at that point -- meaning the person never
 * tapped "Mark as Taken" -- it is auto-flipped to MISSED.
 *
 * This is a no-op if the dose was already confirmed TAKEN, since
 * DatabaseHelper.markDoseMissed() only updates rows that are still
 * PENDING.
 */
public class MissCheckReceiver extends BroadcastReceiver {

    private static final String TAG = "MissCheckReceiver";

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
            return;
        }

        DatabaseHelper databaseHelper = new DatabaseHelper(context);

        boolean flippedToMissed =
                databaseHelper.markDoseMissed(userId, medicineId, scheduledAt);

        Log.i(
                TAG,
                "Miss-check for medicine " + medicineId
                        + " dose " + scheduledAt
                        + ": " + (flippedToMissed ? "MISSED" : "already resolved")
        );
    }
}