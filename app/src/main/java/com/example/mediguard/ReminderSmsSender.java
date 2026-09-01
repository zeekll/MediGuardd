package com.example.mediguard;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

/**
 * Sends the "time to take your medicine" SMS to the reminder's saved
 * contact number when a dose is triggered (step 3).
 */
class ReminderSmsSender {

    private static final String TAG = "ReminderSmsSender";

    static void sendDoseDueSms(
            Context context,
            String contactNumber,
            String medicineName,
            String dose,
            String doseUnit,
            String scheduledTimeDisplay
    ) {

        if (contactNumber == null || contactNumber.trim().isEmpty()) {
            Log.w(TAG, "No contact number saved for this reminder, skipping SMS.");
            return;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "SEND_SMS permission not granted, skipping SMS.");
            return;
        }

        String message =
                "MediGuard Reminder\n"
                        + medicineName + "\n"
                        + "Dose: " + dose + " " + doseUnit + "\n"
                        + "Time: " + scheduledTimeDisplay + "\n"
                        + "It's time to take the medicine.\n"
                        + "Please make sure to take it.";

        try {

            SmsManager smsManager = context.getSystemService(SmsManager.class);

            if (smsManager == null) {
                smsManager = SmsManager.getDefault();
            }

            java.util.ArrayList<String> parts = smsManager.divideMessage(message);

            smsManager.sendMultipartTextMessage(
                    contactNumber,
                    null,
                    parts,
                    null,
                    null
            );

            Log.i(TAG, "Reminder SMS sent to " + contactNumber);

        } catch (Exception e) {
            Log.e(TAG, "Failed to send reminder SMS.", e);
        }
    }
}