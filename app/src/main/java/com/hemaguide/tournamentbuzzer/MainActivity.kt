package com.hemaguide.tournamentbuzzer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hemaguide.tournamentbuzzer.ui.theme.TournamentAlarmTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d("MainActivity", "onCreate called")
        startAlarmService()

        setContent {
            TournamentAlarmTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        onButtonClick = { selectedTone, afterBlowDuration -> playTone(selectedTone, afterBlowDuration) }
                    )
                }
            }
        }

        // Bildschirm im gesperrten Zustand aktiv halten und abdunkeln
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)

        // Bildschirmhelligkeit auf Minimum setzen
        val layoutParams = window.attributes
        layoutParams.screenBrightness = 0.1f  // Set brightness to 10%
        window.attributes = layoutParams
    }

    private fun startAlarmService() {
        Log.d("MainActivity", "Starting AlarmService")
        val intent = Intent(this, AlarmService::class.java)
        startForegroundService(intent)
    }

    private fun playTone(toneType: ToneType, duration: AfterBlowDuration) {
        Log.d("MainActivity", "Sending $toneType action to AlarmService")
        val intent = Intent(this, AlarmService::class.java)
        intent.action = toneType.name
        intent.putExtra("delay", duration)
        intent.putExtra("tone_type", toneType)
        startForegroundService(intent)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("MainActivity", "onKeyDown: keyCode = $keyCode")
        if (keyCode == KeyEvent.KEYCODE_CAMERA || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Log.d("MainActivity", "Key event matched, playing tone")
            playTone(ToneType.FIRST, AfterBlowDuration.ZERO_FIVE)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onButtonClick: (ToneType, AfterBlowDuration) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "HEMA Tournament Buzzer"
        )
        Button(onClick = { onButtonClick(ToneType.FIRST, AfterBlowDuration.ZERO_FIVE) }) {
            Text(text = "Play Sound")
        }

        Spacer(modifier = Modifier.height(16.dp))
        AfterBlowScreen {

        }

        Spacer(modifier = Modifier.height(16.dp))
        AlarmTonePicker {

        }
    }
}

@Composable
fun AfterBlowScreen(modifier: Modifier = Modifier, onButtonClick: () -> Unit){
    var afterBlowExpanded by remember { mutableStateOf(false) }
    var selectedAfterBlowDuration by remember { mutableStateOf(AfterBlowDuration.NONE) }
    val afterBlowOptions = listOf(
        AfterBlowDuration.NONE,
        AfterBlowDuration.ZERO_ONE,
        AfterBlowDuration.ZERO_TWO,
        AfterBlowDuration.ZERO_THREE,
        AfterBlowDuration.ZERO_FOUR,
        AfterBlowDuration.ZERO_FIVE,
        AfterBlowDuration.ZERO_SIX,
        AfterBlowDuration.ZERO_SEVEN,
        AfterBlowDuration.ZERO_EIGHT,
        AfterBlowDuration.ZERO_NINE,
        AfterBlowDuration.ONE_SECOND
    )

    Row {
        Text(
            text = "Afterblow duration: $selectedAfterBlowDuration",
            modifier = Modifier
                .clickable { afterBlowExpanded = true }
                .padding(16.dp)
                .background(Color.Green, shape = MaterialTheme.shapes.medium)
                .padding(16.dp),
            color = MaterialTheme.colorScheme.onPrimary
        )
        DropdownMenu(
            expanded = afterBlowExpanded,
            onDismissRequest = { afterBlowExpanded = false }
        ) {
            afterBlowOptions.forEach { afterblowDuration ->
                DropdownMenuItem(
                    onClick = {
                        selectedAfterBlowDuration = afterblowDuration
                        afterBlowExpanded = false
                    },
                    text = { Text(text = afterblowDuration.duration) }
                )
            }
        }
    }
}

@Composable
fun AlarmTonePicker(modifier: Modifier = Modifier, onButtonClick: () -> Unit){
    var toneExpanded by remember { mutableStateOf(false) }
    var selectedTone by remember { mutableStateOf(ToneType.FIRST) }
    val toneOptions = listOf(
        ToneType.FIRST,
        ToneType.SECOND,
        ToneType.THIRD,
        ToneType.FOURTH
    )

    Box {
        Text(
            text = "Tone: $selectedTone",
            modifier = Modifier
                .clickable { toneExpanded = true }
                .padding(16.dp)
                .background(Color.Cyan, shape = MaterialTheme.shapes.large)
                .padding(16.dp),
            color = MaterialTheme.colorScheme.onPrimary
        )
        DropdownMenu(
            expanded = toneExpanded,
            onDismissRequest = { toneExpanded = false }
        ) {
            toneOptions.forEach { tone ->
                DropdownMenuItem(
                    onClick = {
                        selectedTone = tone
                        toneExpanded = false
                    },
                    text = { Text(text = tone.name) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TournamentAlarmTheme {
//        MainScreen(
//            onButtonClick = {}
//        )
    }
}
