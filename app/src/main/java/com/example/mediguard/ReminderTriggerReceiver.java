package com.example.mediguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Fired by AlarmManager at a dose's scheduled time.
 *
 * Inserts the PENDING dose_log row for this occurrence and starts its
 * miss-check countdown (step 3's data side, which steps 4 and 5 rely
 * on). Sending the actual SMS / showing a heads-up notification is
 * still a TODO -- the status still flips to PENDING and, if unconfirmed,
 * to MISSED, but the person currently has to open the app to see it.
 */
public class ReminderTriggerReceiver extends BroadcastReceiver {

    private static final String TAG = "ReminderTrigger";

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

        Log.i(
                TAG,
                "Reminder fired: user=" + userId
                        + " medicine=" + medicineId
                        + " scheduledAt=" + scheduledAt
        );

        if (userId != -1 && medicineId != -1 && scheduledAt != null) {

            DatabaseHelper databaseHelper = new DatabaseHelper(context);

            // Reminder may have been turned OFF between when this
            // alarm was set and when it fired -- don't create a
            // PENDING dose or miss-check for a reminder that's OFF.
            ReminderConfig config =
                    databaseHelper.getReminderConfig(userId, medicineId);

            if (config != null && config.isEnabled()) {

                databaseHelper.insertDoseLogIfAbsent(
                        userId, medicineId, scheduledAt
                );

                ReminderScheduler.scheduleMissCheck(
                        context,
                        userId,
                        medicineId,
                        scheduledAt,
                        System.currentTimeMillis()
                );

                // TODO (step 3): send the SMS and show the "time to
                // take your medicine" heads-up notification here.
            }
        }

        // Always schedule the following occurrence so the reminder
        // keeps repeating.
        ReminderScheduler.scheduleNext(context, userId, medicineId);
    }
}