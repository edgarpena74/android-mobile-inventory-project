//-----------------------------
//-----------------------------
// SMS NOTIFICATION HELPER
//-----------------------------
//-----------------------------
// Sends low-stock SMS alerts using modern APIs on Android 12+
// and a safe legacy path for older Android versions.

package com.example.cs360projectthreeedgarpena.sms;

//-----------------------------
//-----------------------------
// IMPORTS
//-----------------------------
//-----------------------------

import android.content.Context;
import android.os.Build;
import android.telephony.SmsManager;
import android.telephony.SubscriptionManager;
import android.widget.Toast;

//-----------------------------
//-----------------------------
// SMS SENDER CLASS
//-----------------------------
//-----------------------------
public class SmsSender {

    // Holds app context for toasts and system services
    private final Context context;

    //-----------------------------
    //-----------------------------
    // CONSTRUCTOR
    //-----------------------------
    //-----------------------------
    // Stores context so we can use Toasts and access SmsManager
    public SmsSender(Context context) {
        this.context = context;
    }

    //-----------------------------
    //-----------------------------
    // SEND SMS FUNCTION
    //-----------------------------
    //-----------------------------
    // Sends an SMS to the given number with a text message body.
    // Handles both permission errors and runtime exceptions safely.
    public void sendSms(String phoneNumber, String message) {
        try {
            // Get appropriate SMS manager depending on Android version
            SmsManager smsManager = getSubscriptionAwareSmsManager();

            // Send the text message
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);

            // Notify the user that the message was sent
            Toast.makeText(context, "SMS sent to " + phoneNumber, Toast.LENGTH_SHORT).show();

        } catch (SecurityException se) {
            // Handles missing SEND_SMS permission case
            Toast.makeText(context, "Permission denied: cannot send SMS.", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            // Handles any unexpected runtime error (like invalid number)
            Toast.makeText(context, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //-----------------------------
    //-----------------------------
    // GET SUBSCRIPTION-AWARE SMS MANAGER
    //-----------------------------
    //-----------------------------
    // Chooses the correct SmsManager implementation based on API level.
    // Android 12+ uses getSystemService(), while older versions use the legacy API.
    private SmsManager getSubscriptionAwareSmsManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 (API 31) and newer — modern SMS manager
            return context.getSystemService(SmsManager.class);
        } else {
            //*********
            // !NOTICE!
            //*********
            // The following legacy code path uses deprecated APIs but is still
            // required for devices below Android 12. We suppress this specific
            // warning since it’s intentional and safe.
            @SuppressWarnings("deprecation")
            int subId = SubscriptionManager.getDefaultSmsSubscriptionId();

            @SuppressWarnings("deprecation")
            SmsManager legacyManager = SmsManager.getSmsManagerForSubscriptionId(subId);

            return legacyManager;
        }
    }
}