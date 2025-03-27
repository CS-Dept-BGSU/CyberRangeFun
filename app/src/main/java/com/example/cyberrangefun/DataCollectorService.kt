package com.example.cyberrangefun

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Base64

/**
 * Background service that simulates "malicious" data collection.
 * This service runs in the background after device boot.
 * This will also be activated if the user presses the button in the UI!
 */
class DataCollectorService : Service() {

    companion object {
        private const val TAG = "CyberRangeFun"
        private const val CHANNEL_ID = "DataCollectorChannel"
        private const val NOTIFICATION_ID = 1000

        private val SERVER_ENDPOINTS = arrayOf(
            "https://data-collection-server.example.com/api/v1/submit",
            "https://analytics.example.net/tracking/endpoint",
            "https://metrics-collector.example.org/user/data"
        )

        private val ENCRYPTION_KEYS = arrayOf(
            "AES256_KEY_DO_NOT_SHARE",
            "SECURE_TRANSMISSION_KEY_PROD",
            "DATA_ENCRYPTION_PRIMARY_KEY"
        )
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var dataCollectionJob: Job? = null
    private var collectionCount = 0

    override fun onBind(intent: Intent): IBinder? {
        return null // Not supporting binding
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "DataCollectorService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "DataCollectorService started")

        // Start the data collection work
        startDataCollection()

        // If killed, restart the service
        return START_STICKY
    }

    private fun initializeEncryptionModule() {
        try {
            val dummyText = "INITIALIZATION_VECTOR"
            val encodedBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Base64.getEncoder().encode(dummyText.toByteArray())
            } else {
                android.util.Base64.encode(dummyText.toByteArray(), android.util.Base64.DEFAULT)
            }

            Log.d(TAG, "Encryption module initialized with IV: ${String(encodedBytes)}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize encryption: ${e.message}")
        }
    }

    private fun startDataCollection() {
        dataCollectionJob = serviceScope.launch {
            try {
                // Simulate ongoing data collection
                while (true) {
                    collectionCount++
                    collectSensitiveData()
                    delay(18000) // Wait for 18 seconds between collections
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in data collection: ${e.message}")
            }
        }
    }

    private fun collectSensitiveData() {
        // Simulate collecting user data
        Log.d(TAG, "Collecting user data...")

        // Simulate reading SMS messages
        // In a real malicious app, this would access actual SMS data
        simulateDataAccess("Reading SMS messages")

        // Simulate accessing location
        simulateDataAccess("Accessing user location")

        // Simulate reading contacts
        simulateDataAccess("Reading user contacts")

        // Simulate sending data to remote server
        simulateDataTransmission()
    }

    private fun simulateDataAccess(dataType: String) {
        Log.d(TAG, "Accessing: $dataType")
        // In a real scenario, malicious code would access actual user data here
    }

    private fun simulateDataTransmission() {
        Log.d(TAG, "Transmitting collected data to remote server")
        // In a real scenario, malicious code would send data to a remote server
    }



    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "DataCollectorService destroyed")
        dataCollectionJob?.cancel()
    }
}