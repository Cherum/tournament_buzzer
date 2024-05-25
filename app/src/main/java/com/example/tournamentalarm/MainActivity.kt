package com.example.tournamentalarm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tournamentalarm.ui.theme.TournamentAlarmTheme

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
                        onButtonClick = { playFechtMeldeanlageTone() }
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

    private fun playFechtMeldeanlageTone() {
        Log.d("MainActivity", "Sending PLAY_TONE action to AlarmService")
        val intent = Intent(this, AlarmService::class.java)
        intent.action = "PLAY_TONE"
        startForegroundService(intent)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("MainActivity", "onKeyDown: keyCode = $keyCode")
        if (keyCode == KeyEvent.KEYCODE_CAMERA || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Log.d("MainActivity", "Key event matched, playing tone")
            playFechtMeldeanlageTone()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Greeting(name = "Android")
        Button(onClick = onButtonClick) {
            Text(text = "Play Sound")
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
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
