package com.example.mediguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Fired by AlarmManager at a dose's scheduled time.
 *
 * TODO (step 3): insert the PENDING dose_log row, send the SMS,
 * show the "time to take your medicine" notification, and schedule
 * the miss-check deadline alarm.
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

        // Always schedule the following occurrence so the reminder
        // keeps repeating even before step 3 fills in this trigger's
        // own SMS/notification/dose_log handling.
        ReminderScheduler.scheduleNext(context, userId, medicineId);
    }
}