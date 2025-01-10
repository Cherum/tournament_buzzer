package com.hemaguide.tournamentbuzzer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.content.pm.PackageManager
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

    private val toneDuration = 800

    companion object {
        private const val CHANNEL_ID = "AlarmServiceChannel"
        private const val CHANNEL_NAME = "Alarm Service Channel"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("AlarmService", "Service onCreate")

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        // Start the service in the foreground
        startForegroundService()

        // Initialize ToneGenerator
        initializeToneGenerator()
    }

    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(notificationChannel)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarm Service")
            .setContentText("Alarm service is running")
            .setSmallIcon(R.drawable.logo_schwert_10)
            .build()

        startForeground(1, notification)
    }

    private fun initializeToneGenerator() {
        val isBluetoothConnected = isBluetoothAudioConnected()
        val streamType = if (isBluetoothConnected) {
            AudioManager.STREAM_MUSIC
        } else {
            AudioManager.STREAM_ALARM
        }

        toneGenerator = ToneGenerator(streamType, 100)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("AlarmService", "Service onStartCommand with action: ${intent.action}")
        intent.action?.let {
            try {
                val tone = intent.getSerializableExtra("tone_type") as ToneType
                val sound = getToneGeneratorSound(tone)

                val delay = intent.getSerializableExtra("delay") as AfterBlowDuration
                val toneDelay = getToneDelay(delay)

                playTone(sound, toneDelay)
            } catch (e: Exception) {
                Log.e("AlarmService", "Error in onStartCommand: ${e.message}")
            }
        }

        return START_STICKY
    }

    private fun getToneGeneratorSound(tone: ToneType): Int {
        return when (tone) {
            ToneType.FIRST -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
            ToneType.SECOND -> ToneGenerator.TONE_CDMA_ABBR_ALERT
            ToneType.THIRD -> ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK
        }
    }

    private fun getToneDelay(delay: AfterBlowDuration): Int {
        return when (delay) {
            AfterBlowDuration.NONE -> 0
            AfterBlowDuration.ZERO_ONE -> (0.1 * 1000).toInt()
            AfterBlowDuration.ZERO_TWO -> (0.2 * 1000).toInt()
            AfterBlowDuration.ZERO_THREE -> (0.3 * 1000).toInt()
            AfterBlowDuration.ZERO_FOUR -> (0.4 * 1000).toInt()
            AfterBlowDuration.ZERO_FIVE -> (0.5 * 1000).toInt()
            AfterBlowDuration.ZERO_SIX -> (0.6 * 1000).toInt()
            AfterBlowDuration.ZERO_SEVEN -> (0.7 * 1000).toInt()
            AfterBlowDuration.ZERO_EIGHT -> (0.8 * 1000).toInt()
            AfterBlowDuration.ZERO_NINE -> (0.9 * 1000).toInt()
            AfterBlowDuration.ONE_SECOND -> (1 * 1000).toInt()
        }
    }

    fun playTone(toneType: Int, toneDelay: Int) {
        Log.d("AlarmService", "Playing tone")
        if (isPlaying) {
            return
        }
        isPlaying = true

        // Introduce a delay before playing the tone to ensure Bluetooth connection is ready
        handler.postDelayed({
            toneGenerator.startTone(toneType, toneDuration)

            // Set a delay to reset isPlaying after the tone duration
            handler.postDelayed({
                isPlaying = false
                Log.d("AlarmService", "Tone finished playing")
            }, toneDuration.toLong())
        }, toneDelay.toLong())
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
            try {
                if (checkSelfPermission(android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                    val devices = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP)
                    if (devices == BluetoothProfile.STATE_CONNECTED) {
                        return true
                    }
                } else {
                    Log.w("AlarmService", "Bluetooth permission not granted")
                }
            } catch (e: SecurityException) {
                Log.e("AlarmService", "SecurityException: ${e.message}")
            }
        }
        return false
    }
}