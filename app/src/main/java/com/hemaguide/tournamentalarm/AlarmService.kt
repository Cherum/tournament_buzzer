package com.hemaguide.tournamentalarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat

class AlarmService : Service() {

    private lateinit var toneGenerator: ToneGenerator
    private lateinit var audioManager: AudioManager
    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())

    private val delayBeforeTone = 1000L // 500ms delay before playing the tone
    private val toneDuration = 1500 // 2500ms = 2.5 seconds

    override fun onCreate() {
        super.onCreate()
        Log.d("AlarmService", "Service onCreate")

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        // Start the service in the foreground
        startForegroundService()

        // Check if a Bluetooth audio device is connected
        val isBluetoothConnected = isBluetoothAudioConnected()

        // Select the appropriate audio stream
        val streamType = if (isBluetoothConnected) {
            AudioManager.STREAM_MUSIC
        } else {
            AudioManager.STREAM_ALARM
        }

        toneGenerator = ToneGenerator(streamType, 100)
    }

    private fun startForegroundService() {
        val channelId = "AlarmServiceChannel"
        val channelName = "Alarm Service Channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(notificationChannel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Alarm Service")
            .setContentText("Alarm service is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("AlarmService", "Service onStartCommand with action: ${intent?.action}")
        if (intent?.action == "PLAY_TONE") {
            if (!isPlaying) {
                playTone()
            } else {
                Log.d("AlarmService", "Tone is already playing")
            }
        }
        return START_STICKY
    }

    private fun playTone() {
        Log.d("AlarmService", "Playing tone")
        isPlaying = true

        // Introduce a delay before playing the tone to ensure Bluetooth connection is ready
        handler.postDelayed({
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, toneDuration)

            // Set a delay to reset isPlaying after the tone duration
            handler.postDelayed({
                isPlaying = false
                Log.d("AlarmService", "Tone finished playing")
            }, toneDuration.toLong())
        }, delayBeforeTone)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("AlarmService", "Service onDestroy")
        toneGenerator.release()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun isBluetoothAudioConnected(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
            val devices = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP)
            if (devices == BluetoothProfile.STATE_CONNECTED) {
                return true
            }
        }
        return false
    }
}
