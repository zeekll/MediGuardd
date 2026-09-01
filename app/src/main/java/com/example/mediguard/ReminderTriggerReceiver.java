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
 * Step 3: inserts the PENDING dose_log row, sends the SMS, shows
 * the "time to take your medicine" notification, and schedules the
 * miss-check deadline alarm.
 */
public class ReminderTriggerReceiver extends BroadcastReceiver {

    private static final String TAG = "ReminderTrigger";

    private static final SimpleDateFormat DISPLAY_TIME_FORMAT =
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
            handleDoseTriggered(context, userId, medicineId, scheduledAt);
        }

        // Always schedule the following occurrence so the reminder
        // keeps repeating regardless of how this occurrence's own
        // SMS/notification/dose_log handling above turned out.
        ReminderScheduler.scheduleNext(context, userId, medicineId);
    }

    private void handleDoseTriggered(
            Context context,
            int userId,
            int medicineId,
            String scheduledAt
    ) {

        DatabaseHelper databaseHelper = new DatabaseHelper(context);

        // Status automatically changes to PENDING when the scheduled
        // time is reached. insertDoseLogIfAbsent() is a no-op if this
        // occurrence was already logged (e.g. receiver re-delivery).
        databaseHelper.insertDoseLogIfAbsent(userId, medicineId, scheduledAt);

        ReminderConfig config =
                databaseHelper.getReminderConfig(userId, medicineId);

        String medicineName = databaseHelper.getMedicineName(medicineId);

        if (config == null || medicineName == null) {
            Log.e(
                    TAG,
                    "Missing reminder config or medicine, skipping SMS/notification."
            );
            return;
        }

        String scheduledTimeDisplay = formatDisplayTime(scheduledAt);

        ReminderSmsSender.sendDoseDueSms(
                context,
                config.getContactNumber(),
                medicineName,
                config.getDose(),
                config.getDoseUnit(),
                scheduledTimeDisplay
        );

        ReminderNotifier.showDoseDueNotification(
                context,
                medicineId,
                medicineName,
                config.getDose(),
                config.getDoseUnit(),
                scheduledTimeDisplay
        );

        // Kung hindi ma-confirm sa loob ng time window, automatic na
        // magiging MISSED ang dose (step 5).
        ReminderScheduler.scheduleMissCheck(context, userId, medicineId, scheduledAt);
    }

    private String formatDisplayTime(String scheduledAt) {

        try {

            Date parsed = new SimpleDateFormat(
                    ReminderScheduler.SCHEDULED_AT_FORMAT, Locale.US
            ).parse(scheduledAt);

            return parsed != null
                    ? DISPLAY_TIME_FORMAT.format(parsed)
                    : scheduledAt;

        } catch (ParseException e) {
            return scheduledAt;
        }
    }
}