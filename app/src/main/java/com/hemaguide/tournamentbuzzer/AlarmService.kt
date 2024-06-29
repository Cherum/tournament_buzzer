package com.hemaguide.tournamentbuzzer

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

    private val toneDuration = 1500

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
            .setSmallIcon(R.drawable.logo_schwert_10U)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("AlarmService", "Service onStartCommand with action: ${intent.action}")
        if (intent.action != null) {
            val tone = intent.getSerializableExtra("tone_type") as ToneType
            val sound = when (tone) {
                ToneType.FIRST -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
                ToneType.SECOND -> ToneGenerator.TONE_CDMA_ABBR_ALERT
                ToneType.THIRD -> ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK
                ToneType.FOURTH -> ToneGenerator.TONE_CDMA_HIGH_L
            }

            val delay = intent.getSerializableExtra("delay") as AfterBlowDuration
            val toneDelay = when(delay){
                AfterBlowDuration.NONE -> 0
                AfterBlowDuration.ZERO_ONE -> 0.1 * 1000
                AfterBlowDuration.ZERO_TWO -> 0.2 * 1000
                AfterBlowDuration.ZERO_THREE -> 0.3 * 1000
                AfterBlowDuration.ZERO_FOUR -> 0.4 * 1000
                AfterBlowDuration.ZERO_FIVE -> 0.5 * 1000
                AfterBlowDuration.ZERO_SIX -> 0.6 * 1000
                AfterBlowDuration.ZERO_SEVEN -> 0.7 * 1000
                AfterBlowDuration.ZERO_EIGHT -> 0.8 * 1000
                AfterBlowDuration.ZERO_NINE -> 0.9 * 1000
                AfterBlowDuration.ONE_SECOND -> 1 * 1000
            }

            playTone(sound, toneDelay)
        }

        return START_STICKY
    }

    /**
     * Play a tone for the specified duration.
     * toneDuration in milliseconds
     */
    private fun playTone(toneType: Int, toneDelay: Number) {
        Log.d("AlarmService", "Playing tone")
        if (isPlaying){
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
            val devices = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP)
            if (devices == BluetoothProfile.STATE_CONNECTED) {
                return true
            }
        }
        return false
    }
}
