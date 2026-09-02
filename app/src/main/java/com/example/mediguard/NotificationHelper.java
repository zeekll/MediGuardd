package com.example.mediguard;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * Step 3's "Time to take your medicine" heads-up notification.
 * Tapping it opens the dose's schedule screen (step 2 / step 4).
 */
public class NotificationHelper {

    private static final String CHANNEL_ID = "mediguard_reminders";
    private static final String CHANNEL_NAME = "Medicine Reminders";

    public static void ensureChannel(Context context) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager =
                context.getSystemService(NotificationManager.class);

        if (manager == null) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );

        channel.setDescription(
                "Alerts you when it's time to take a saved medicine."
        );

        manager.createNotificationChannel(channel);
    }

    public static void showDoseReminder(
            Context context,
            int userId,
            int medicineId,
            String medicineName,
            String medicineType,
            String doseText,
            String timeLabel
    ) {

        ensureChannel(context);

        Intent intent = new Intent(context, ReminderDetailsActivity.class);

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        intent.putExtra("user_id", userId);
        intent.putExtra("medicine_id", medicineId);
        intent.putExtra("medicine_name", medicineName);
        intent.putExtra("medicine_type", medicineType);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                medicineId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_alarm)
                        .setContentTitle("Time to take your medicine")
                        .setContentText(
                                medicineName + " • " + doseText
                                        + " • " + timeLabel
                        )
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(
                                medicineName + "\nDose: " + doseText
                                        + "\nScheduled: " + timeLabel
                        ))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {
            // Not granted -- silently skip the notification. The dose
            // still turns PENDING/MISSED normally; the person just
            // won't get a heads-up until they grant the permission.
            return;
        }

        NotificationManagerCompat.from(context)
                .notify(medicineId, builder.build());
    }
}