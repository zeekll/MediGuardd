package com.example.mediguard;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

/**
 * Posts the two local notifications the reminder flow needs:
 *  - "time to take your medicine", when a dose is triggered (step 3)
 *  - "missed dose", when a PENDING dose isn't confirmed in time (step 5)
 */
public class ReminderNotifier {

    private static final String CHANNEL_ID = "mediguard_reminders";
    private static final String CHANNEL_NAME = "Medicine Reminders";

    static void showDoseDueNotification(
            Context context,
            int medicineId,
            String medicineName,
            String dose,
            String doseUnit,
            String scheduledTimeDisplay
    ) {

        ensureChannel(context);

        String body =
                "Dose: " + dose + " " + doseUnit
                        + "\nTime: " + scheduledTimeDisplay
                        + "\nIt's time to take the medicine. Please make sure to take it.";

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notifications)
                        .setContentTitle(medicineName)
                        .setContentText("Time to take your medicine")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setAutoCancel(true)
                        .setContentIntent(openAppPendingIntent(context, medicineId));

        notify(context, doseDueNotificationId(medicineId), builder);
    }

    static void showMissedDoseNotification(
            Context context,
            int medicineId,
            String medicineName,
            String scheduledAt
    ) {

        ensureChannel(context);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_warning)
                        .setContentTitle("Missed Dose")
                        .setContentText(
                                medicineName + " was not confirmed as taken in time."
                        )
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setAutoCancel(true)
                        .setContentIntent(openAppPendingIntent(context, medicineId));

        notify(context, missedDoseNotificationId(medicineId), builder);
    }

    private static void notify(
            Context context,
            int notificationId,
            NotificationCompat.Builder builder
    ) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            // Notification permission not granted; nothing more we can do.
            return;
        }

        try {
            NotificationManagerCompat.from(context)
                    .notify(notificationId, builder.build());
        } catch (SecurityException ignored) {
            // Permission revoked between the check above and the call.
        }
    }

    private static PendingIntent openAppPendingIntent(Context context, int medicineId) {

        Intent intent = new Intent(context, MedicationReminderActivity.class);
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        return PendingIntent.getActivity(
                context,
                medicineId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static void ensureChannel(Context context) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );

        channel.setDescription("Alerts for scheduled and missed medicine doses.");

        NotificationManager manager =
                context.getSystemService(NotificationManager.class);

        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private static int doseDueNotificationId(int medicineId) {
        return medicineId * 10 + 3;
    }

    private static int missedDoseNotificationId(int medicineId) {
        return medicineId * 10 + 4;
    }
}