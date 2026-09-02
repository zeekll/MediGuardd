package com.example.mediguard;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.ActivityCompat;

/**
 * Step 3's one-way SMS: "MediGuard Reminder ... It's time to take
 * the medicine. Please make sure to take it."
 */
public class SmsHelper {

    private static final String TAG = "SmsHelper";

    public static void sendDoseReminder(
            Context context,
            String contactNumber,
            String medicineName,
            String doseText,
            String timeLabel
    ) {

        if (TextUtils.isEmpty(contactNumber)) {
            Log.w(TAG, "No contact number saved, skipping SMS.");
            return;
        }

        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.SEND_SMS
        ) != PackageManager.PERMISSION_GRANTED) {

            // Not granted -- silently skip. The dose/notification
            // flow still works; only the SMS leg is unavailable
            // until the person grants SEND_SMS.
            Log.w(TAG, "SEND_SMS not granted, skipping SMS.");
            return;
        }

        String message =
                "MediGuard Reminder\n"
                        + medicineName + "\n"
                        + "Dose: " + doseText + "\n"
                        + "Time: " + timeLabel + "\n\n"
                        + "It's time to take the medicine. Please make sure to take it.";

        try {

            SmsManager smsManager = context.getSystemService(SmsManager.class);

            if (smsManager == null) {
                smsManager = SmsManager.getDefault();
            }

            // Long messages need to be split into multiple SMS parts.
            java.util.ArrayList<String> parts =
                    smsManager.divideMessage(message);

            smsManager.sendMultipartTextMessage(
                    contactNumber, null, parts, null, null
            );

        } catch (Exception e) {
            Log.e(TAG, "Failed to send SMS reminder.", e);
        }
    }
}