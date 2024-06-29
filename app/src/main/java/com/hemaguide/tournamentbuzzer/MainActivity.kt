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
                        onButtonClick = { selectedTone -> playFechtMeldeanlageTone(selectedTone) }
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

    private fun playFechtMeldeanlageTone(toneType: String) {
        Log.d("MainActivity", "Sending $toneType action to AlarmService")
        val intent = Intent(this, AlarmService::class.java)
        intent.action = toneType
        startForegroundService(intent)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("MainActivity", "onKeyDown: keyCode = $keyCode")
        if (keyCode == KeyEvent.KEYCODE_CAMERA || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Log.d("MainActivity", "Key event matched, playing tone")
            playFechtMeldeanlageTone("PLAY_TONE_FIRST")
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onButtonClick: (String) -> Unit
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
        Button(onClick = { onButtonClick("PLAY_TONE_FIRST") }) {
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
    var selectedAfterblowDuration by remember { mutableStateOf("None") }
    val afterblowOptions = listOf("None", "0.1 s", "0.2 s", "0.3 s", "0.4 s", "0.5 s", "0.6 s", "0.7 s", "0.8 s", "0.9 s", "1 s")

    Row {
        Text(
            text = "Afterblow duration: $selectedAfterblowDuration",
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
            afterblowOptions.forEach { afterblowDuration ->
                DropdownMenuItem(
                    onClick = {
                        selectedAfterblowDuration = afterblowDuration
                        afterBlowExpanded = false
                    },
                    text = { Text(text = afterblowDuration) }
                )
            }
        }
    }
}

@Composable
fun AlarmTonePicker(onButtonClick: () -> Unit){
    var toneExpanded by remember { mutableStateOf(false) }
    var selectedTone by remember { mutableStateOf("First") }
    val toneOptions = listOf("First", "Second", "Third", "Fourth")

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
                    text = { Text(text = tone) }
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
            onButtonClick = {}
        )
    }
}
