package com.example.mediguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fired by AlarmManager at a dose's scheduled time.
 *
 * Inserts the PENDING dose_log row for this occurrence, starts its
 * miss-check countdown (needed for steps 4 & 5), sends the one-way
 * SMS to the saved contact number, and shows the "time to take your
 * medicine" heads-up notification.
 */
public class ReminderTriggerReceiver extends BroadcastReceiver {

    private static final String TAG = "ReminderTrigger";

    private static final SimpleDateFormat SCHEDULED_AT_FORMAT =
            new SimpleDateFormat(ReminderScheduler.SCHEDULED_AT_FORMAT, Locale.US);

    private static final SimpleDateFormat TIME_LABEL_FORMAT =
            new SimpleDateFormat("h:mm a", Locale.US);

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
            // PENDING dose, miss-check, SMS, or notification for a
            // reminder that's OFF.
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

                String medicineName =
                        databaseHelper.getMedicineName(medicineId);

                if (medicineName == null) {
                    medicineName = "Your medicine";
                }

                String doseText = config.getDose() + " " + config.getDoseUnit();
                String timeLabel = formatTimeLabel(scheduledAt);

                SmsHelper.sendDoseReminder(
                        context,
                        config.getContactNumber(),
                        medicineName,
                        doseText,
                        timeLabel
                );

                NotificationHelper.showDoseReminder(
                        context,
                        userId,
                        medicineId,
                        medicineName,
                        null,
                        doseText,
                        timeLabel
                );
            }
        }

        // Always schedule the following occurrence so the reminder
        // keeps repeating.
        ReminderScheduler.scheduleNext(context, userId, medicineId);
    }

    private String formatTimeLabel(String scheduledAt) {

        try {

            Date date = SCHEDULED_AT_FORMAT.parse(scheduledAt);
            return TIME_LABEL_FORMAT.format(date);

        } catch (ParseException e) {
            return scheduledAt;
        }
    }
}