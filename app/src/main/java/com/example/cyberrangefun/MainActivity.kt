package com.example.cyberrangefun

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CyberRangeFun"
        private const val CHANNEL_ID = "CyberRangeFunChannel"
        private const val NOTIFICATION_ID = 1
        private const val EXTRA_FROM_NOTIFICATION = "from_notification"
        private const val EXTRA_SHOW_HACKED = "show_hacked_message"
    }
    private var hackedMessageShown = false
    private var notificationSent  = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Create notification channel (required for Android 8.0+)
        createNotificationChannel()

        // Find UI elements
        val statusText = findViewById<TextView>(R.id.statusText)
        val startButton = findViewById<Button>(R.id.startButton)

        // Only show notification settings dialog at startup - don't show if returning from background
        if (intent.getBooleanExtra(EXTRA_SHOW_HACKED, false)) {
            // Coming from notification - show hacked message immediately
            showHackedMessage(statusText, startButton)

            // Only show notification settings if needed and not coming from notification
            if (!intent.getBooleanExtra(EXTRA_FROM_NOTIFICATION, false)) {
                Handler(Looper.getMainLooper()).postDelayed({
                    showNotificationSettingsPrompt()
                }, 1000)
            }
        } else {
            // Normal app launch

            // Show notification settings prompt
            Handler(Looper.getMainLooper()).postDelayed({
                showNotificationSettingsPrompt()
            }, 1000)

            // Schedule the hacked message if it hasn't been shown
            if (!hackedMessageShown) {
                scheduleHackedMessage(statusText, startButton)
            }
        }

        // Set up start button
        startButton.setOnClickListener {
            if (!hackedMessageShown) {
                statusText.text = "You are cool!"
            }
        }
    }

    private fun scheduleHackedMessage(statusText: TextView, startButton: Button) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (!hackedMessageShown) {
                showHackedMessage(statusText, startButton)

                // Only send notification if it hasn't been sent yet
                if (!notificationSent) {
                    showNotification()
                    notificationSent = true
                }
            }
        }, 15000)
    }

    private fun showHackedMessage(statusText: TextView, startButton: Button) {
        hackedMessageShown = true

        // Get the base messages
        val hackedLol = "Hacked lol!"
        val dataMessages = "\n${getString(R.string.sms_read)}\n" +
                "${getString(R.string.location_tracked)}\n" +
                "${getString(R.string.contacts_copied)}\n" +
                "${getString(R.string.photos_scanned)}\n" +
                getString(R.string.history_recorded)

        // Create a SpannableString for styling different parts of the text
        val fullMessage = SpannableString(hackedLol + dataMessages)

        // Make the "Hacked lol!" part normal size
        fullMessage.setSpan(
            RelativeSizeSpan(1.0f),
            0,
            hackedLol.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Make the details smaller (70% of normal size)
        fullMessage.setSpan(
            RelativeSizeSpan(0.7f),
            hackedLol.length,
            fullMessage.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Set the formatted text
        statusText.text = fullMessage

        startButton.isEnabled = false
    }

    private fun showNotificationSettingsPrompt() {
        try {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.notification_prompt_title))
                .setMessage(getString(R.string.notification_prompt_message))
                .setPositiveButton(getString(R.string.open_settings)) { _, _ ->
                    openAppNotificationSettings()
                }
                .setNegativeButton(getString(R.string.later)) { dialog, _ ->
                    dialog.dismiss()
                    Toast.makeText(
                        this,
                        getString(R.string.notification_denied_message),
                        Toast.LENGTH_LONG
                    ).show()
                }
                .setCancelable(false)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification dialog: ${e.message}")
        }
    }

    private fun openAppNotificationSettings() {
        // This opens the specific notification settings for this app
        val intent = Intent()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        } else {
            // For Android 7.1 and below
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse("package:$packageName")
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening settings: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "CyberRangeFun Notifications"
            val descriptionText = "Notifications from CyberRangeFun app"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification() {
        // Create an intent that opens this activity when notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_FROM_NOTIFICATION, true)
            putExtra(EXTRA_SHOW_HACKED, true)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build the notification
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Show the notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    // Add this to handle the case when activity is already running
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        // Check if we're coming from notification
        if (intent.getBooleanExtra(EXTRA_FROM_NOTIFICATION, false)) {
            // Coming from notification, show hacked message immediately
            val statusText = findViewById<TextView>(R.id.statusText)
            val startButton = findViewById<Button>(R.id.startButton)
            showHackedMessage(statusText, startButton)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("hackedMessageShown", hackedMessageShown)
        outState.putBoolean("notificationSent", notificationSent)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        hackedMessageShown = savedInstanceState.getBoolean("hackedMessageShown", false)
        notificationSent = savedInstanceState.getBoolean("notificationSent", false)
    }
}