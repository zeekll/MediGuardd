package com.example.mediguard;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Schedules and cancels the AlarmManager alarm for the next dose of
 * one medicine's reminder. Only ever keeps ONE trigger alarm pending
 * per medicine -- TriggerReceiver re-calls scheduleNext() for the
 * following occurrence once it handles the current one.
 */
public class ReminderScheduler {

    private static final String TAG = "ReminderScheduler";

    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_MEDICINE_ID = "medicine_id";
    public static final String EXTRA_SCHEDULED_AT = "scheduled_at";

    public static final String SCHEDULED_AT_FORMAT = "yyyy-MM-dd HH:mm";

    /**
     * How long a dose stays PENDING before it auto-flips to MISSED
     * (matches "Missed Dose" copy: "hindi na-confirm sa loob ng
     * time window").
     */
    public static final int MISS_WINDOW_MINUTES = 30;

    /**
     * Reads this medicine's saved reminder, finds its next dose
     * occurrence after now, and schedules an exact alarm for it.
     * If the reminder is missing, disabled, or its active window
     * has ended, any existing alarm for it is cancelled instead.
     */
    public static void scheduleNext(
            Context context,
            int userId,
            int medicineId
    ) {

        DatabaseHelper databaseHelper = new DatabaseHelper(context);

        ReminderConfig config =
                databaseHelper.getReminderConfig(userId, medicineId);

        if (config == null || !config.isEnabled()) {
            cancel(context, medicineId);
            return;
        }

        Calendar next =
                ReminderScheduleCalculator.findNextOccurrence(
                        config,
                        Calendar.getInstance()
                );

        if (next == null) {
            cancel(context, medicineId);
            return;
        }

        String scheduledAt =
                new SimpleDateFormat(SCHEDULED_AT_FORMAT, Locale.US)
                        .format(next.getTime());

        Intent intent = new Intent(context, ReminderTriggerReceiver.class);

        intent.putExtra(EXTRA_USER_ID, userId);
        intent.putExtra(EXTRA_MEDICINE_ID, medicineId);
        intent.putExtra(EXTRA_SCHEDULED_AT, scheduledAt);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        triggerRequestCode(medicineId),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager unavailable, cannot schedule reminder.");
            return;
        }

        setExactAlarm(alarmManager, next.getTimeInMillis(), pendingIntent);

        Log.i(
                TAG,
                "Scheduled next dose for medicine "
                        + medicineId
                        + " at "
                        + scheduledAt
        );
    }

    /**
     * Cancels the pending trigger alarm for a medicine's reminder.
     * Called when a reminder is turned off, edited (before it is
     * re-scheduled), or deleted.
     */
    public static void cancel(Context context, int medicineId) {

        Intent intent = new Intent(context, ReminderTriggerReceiver.class);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        triggerRequestCode(medicineId),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        pendingIntent.cancel();
    }

    /**
     * Schedules the "still not confirmed" deadline check for one
     * dose, MISS_WINDOW_MINUTES after it was triggered. Fired by
     * ReminderTriggerReceiver right after it creates the PENDING
     * dose_log row.
     */
    public static void scheduleMissCheck(
            Context context,
            int userId,
            int medicineId,
            String scheduledAt,
            long triggeredAtMillis
    ) {

        Intent intent = new Intent(context, MissCheckReceiver.class);

        intent.putExtra(EXTRA_USER_ID, userId);
        intent.putExtra(EXTRA_MEDICINE_ID, medicineId);
        intent.putExtra(EXTRA_SCHEDULED_AT, scheduledAt);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        missCheckRequestCode(medicineId),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager unavailable, cannot schedule miss-check.");
            return;
        }

        long triggerAtMillis =
                triggeredAtMillis
                        + (MISS_WINDOW_MINUTES * 60_000L);

        setExactAlarm(alarmManager, triggerAtMillis, pendingIntent);
    }

    /**
     * Pushes a dose's miss-check deadline "minutes" further into the
     * future from right now (used by the "Snooze 15 min" action on
     * the reminder detail screen). The dose stays PENDING either way
     * -- this only buys more time before it can auto-flip to MISSED.
     */
    public static void snoozeMissCheck(
            Context context,
            int userId,
            int medicineId,
            String scheduledAt,
            int minutes
    ) {

        Intent intent = new Intent(context, MissCheckReceiver.class);

        intent.putExtra(EXTRA_USER_ID, userId);
        intent.putExtra(EXTRA_MEDICINE_ID, medicineId);
        intent.putExtra(EXTRA_SCHEDULED_AT, scheduledAt);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        missCheckRequestCode(medicineId),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager unavailable, cannot snooze miss-check.");
            return;
        }

        long triggerAtMillis =
                System.currentTimeMillis() + (minutes * 60_000L);

        setExactAlarm(alarmManager, triggerAtMillis, pendingIntent);
    }

    /**
     * Cancels the pending miss-check deadline for a medicine. Called
     * once a dose is confirmed Taken (or snoozed/rescheduled), so a
     * dose that was already confirmed can't later flip to Missed.
     */
    public static void cancelMissCheck(Context context, int medicineId) {

        Intent intent = new Intent(context, MissCheckReceiver.class);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        missCheckRequestCode(medicineId),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        pendingIntent.cancel();
    }

    private static void setExactAlarm(
            AlarmManager alarmManager,
            long triggerAtMillis,
            PendingIntent pendingIntent
    ) {

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    && !alarmManager.canScheduleExactAlarms()) {

                // No exact-alarm permission granted; fall back to an
                // inexact alarm rather than crash or silently drop it.
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                );

                return;
            }

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
            );

        } catch (SecurityException e) {

            Log.e(TAG, "Exact alarm permission denied, using inexact alarm.", e);

            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
            );
        }
    }

    /**
     * Stable per-medicine request code. medicine_id is UNIQUE on the
     * reminders table (one reminder per medicine), so this is safe.
     */
    static int triggerRequestCode(int medicineId) {
        return medicineId * 10 + 1;
    }

    static int missCheckRequestCode(int medicineId) {
        return medicineId * 10 + 2;
    }
}