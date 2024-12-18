package com.hemaguide.tournamentbuzzer

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hemaguide.tournamentbuzzer.ui.theme.TournamentAlarmTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    companion object {
        private const val TAG = "MainActivity"
        private const val SCREEN_BRIGHTNESS = 0.1f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        Log.d(TAG, "onCreate called")
        startAlarmService()

        setContent {
            TournamentAlarmTheme {
                val tone by viewModel.tone.collectAsState()
                val afterBlowDuration by viewModel.afterBlowDuration.collectAsState()
                val afterBlowExpanded by viewModel.afterBlowExpanded.collectAsState()
                val toneExpanded by viewModel.toneExpanded.collectAsState()
                var playToneTrigger by remember { mutableStateOf(false) }

                if (playToneTrigger) {
                    playTone(tone, afterBlowDuration)
                    playToneTrigger = false
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        tone = tone,
                        afterBlowDuration = afterBlowDuration,
                        afterBlowExpanded = afterBlowExpanded,
                        toneExpanded = toneExpanded,
                        onToneChanged = { viewModel.setTone(it) },
                        onDurationChanged = { viewModel.setAfterBlowDuration(it) },
                        onAfterBlowExpandedChanged = { viewModel.setAfterBlowExpanded(it) },
                        onToneExpandedChanged = { viewModel.setToneExpanded(it) },
                        onButtonClick = { playToneTrigger = true }
                    )
                }
            }
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)

        val layoutParams = window.attributes
        layoutParams.screenBrightness = SCREEN_BRIGHTNESS
        window.attributes = layoutParams
    }

    private fun startAlarmService() {
        Log.d(TAG, "Starting AlarmService")
        val intent = Intent(this, AlarmService::class.java)
        startForegroundService(intent)
    }

    private fun playTone(toneType: ToneType, duration: AfterBlowDuration) {
        Log.d(TAG, "Sending $toneType action to AlarmService")
        val intent = Intent(this, AlarmService::class.java).apply {
            action = toneType.name
            putExtra("delay", duration)
            putExtra("tone_type", toneType)
        }
        startForegroundService(intent)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d(TAG, "onKeyDown: keyCode = $keyCode")
        if (keyCode == KeyEvent.KEYCODE_CAMERA || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Log.d(TAG, "Key event matched, playing tone")
            val tone = viewModel.tone.value
            val afterBlowDuration = viewModel.afterBlowDuration.value
            playTone(tone, afterBlowDuration)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    tone: ToneType,
    afterBlowDuration: AfterBlowDuration,
    afterBlowExpanded: Boolean,
    toneExpanded: Boolean,
    onDurationChanged: (AfterBlowDuration) -> Unit,
    onToneChanged: (ToneType) -> Unit,
    onAfterBlowExpandedChanged: (Boolean) -> Unit,
    onToneExpandedChanged: (Boolean) -> Unit,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "HEMA Tournament Buzzer")

        Spacer(modifier = Modifier.height(16.dp))
        AfterBlowScreen(
            duration = afterBlowDuration,
            expanded = afterBlowExpanded,
            onDurationChanged = onDurationChanged,
            onExpandedChanged = onAfterBlowExpandedChanged
        )

        Spacer(modifier = Modifier.height(16.dp))
        AlarmTonePicker(
            tone = tone,
            expanded = toneExpanded,
            onTypeChanged = onToneChanged,
            onExpandedChanged = onToneExpandedChanged
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onButtonClick) {
            Text(text = "Play Sound")
        }
    }
}

@Composable
fun AfterBlowScreen(
    duration: AfterBlowDuration,
    expanded: Boolean,
    onDurationChanged: (AfterBlowDuration) -> Unit,
    onExpandedChanged: (Boolean) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Afterblow Delay: ${duration.duration}",
            modifier = Modifier
                .clickable { onExpandedChanged(true) }
                .background(Color.Green, shape = MaterialTheme.shapes.medium)
                .padding(16.dp),
            color = MaterialTheme.colorScheme.onPrimary
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChanged(false) }
        ) {
            AFTER_BLOW_OPTIONS.forEach { afterblowDuration ->
                DropdownMenuItem(
                    onClick = {
                        onExpandedChanged(false)
                        onDurationChanged(afterblowDuration)
                    },
                    text = { Text(text = afterblowDuration.duration) }
                )
            }
        }
    }
}

@Composable
fun AlarmTonePicker(
    tone: ToneType,
    expanded: Boolean,
    onTypeChanged: (ToneType) -> Unit,
    onExpandedChanged: (Boolean) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Tone: $tone",
            modifier = Modifier
                .clickable { onExpandedChanged(true) }
                .background(Color.Cyan, shape = MaterialTheme.shapes.medium)
                .padding(16.dp),
            color = MaterialTheme.colorScheme.onPrimary
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChanged(false) }
        ) {
            TONE_OPTIONS.forEach { toneOption ->
                DropdownMenuItem(
                    onClick = {
                        onExpandedChanged(false)
                        onTypeChanged(toneOption)
                    },
                    text = { Text(text = toneOption.name) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TournamentAlarmTheme {
        MainScreen(
            tone = ToneType.SECOND,
            afterBlowDuration = AfterBlowDuration.ZERO_TWO,
            onDurationChanged = {},
            onToneChanged = {},
            onToneExpandedChanged = {},
            onAfterBlowExpandedChanged = { },
            toneExpanded = false,
            afterBlowExpanded = false,
            onButtonClick = {}
        )
    }
}