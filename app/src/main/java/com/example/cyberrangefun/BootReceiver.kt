package com.example.cyberrangefun

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * This BroadcastReceiver automatically starts when the device boots up.
 * It launches the DataCollectorService in the background.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "CyberRangeFun"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "BootReceiver: onReceive called with action: ${intent.action}")

        if (Intent.ACTION_BOOT_COMPLETED == intent.action ||
            "android.intent.action.QUICKBOOT_POWERON" == intent.action) {

            Log.d(TAG, "Boot completed received, starting service and MainActivity")

            // Start the background service
            val serviceIntent = Intent(context, DataCollectorService::class.java)
            context.startService(serviceIntent)

            // Open MainActivity with flag to request notification settings
            val mainActivityIntent = Intent(context, MainActivity::class.java)
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            mainActivityIntent.putExtra("OPEN_NOTIFICATION_SETTINGS", true)

            // Add these logs to verify the intent is being created correctly
            Log.d(TAG, "Starting MainActivity with OPEN_NOTIFICATION_SETTINGS=true")

            try {
                context.startActivity(mainActivityIntent)
                Log.d(TAG, "MainActivity started successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting MainActivity: ${e.message}")
            }
        }
    }
}