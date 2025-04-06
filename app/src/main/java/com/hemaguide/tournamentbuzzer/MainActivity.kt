package com.hemaguide.tournamentbuzzer

import android.Manifest
import android.app.Fragment
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.hemaguide.tournamentbuzzer.ui.theme.TournamentAlarmTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    companion object {
        private const val TAG = "MainActivity"
        private const val SCREEN_BRIGHTNESS = 0.1f
    }

    private val requestBluetoothPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted: Proceed with Bluetooth functionality
                startAlarmService()
            } else {
                // Permission denied: Handle the case where the user denies the permission
                // For example, show a message explaining why the permission is needed
                Log.e("MainActivity","Bluetooth permission denied")
            }
        }

    fun checkBluetoothPermissionAndStartService(activity: MainActivity, fragment: Fragment? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // For Android 12 (S) and above
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // BLUETOOTH_CONNECT permission is granted
                startAlarmService()
            } else {
                // Request BLUETOOTH_CONNECT permission
                requestBluetoothPermissions(activity, fragment, arrayOf(Manifest.permission.BLUETOOTH_CONNECT))
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // For devices below Android 12 (S) but above or equal to Android 6 (M)
            // Check if both BLUETOOTH and BLUETOOTH_ADMIN are granted
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
                // Both permissions granted
                startAlarmService()
            } else {
                // Request both permissions if they are not granted
                requestBluetoothPermissions(activity, fragment, arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN))
            }
        }else{
            // For devices below Android 6 (M) no runtime permissions needed
            startAlarmService()
        }
    }

    private fun requestBluetoothPermissions(activity: MainActivity, fragment: Fragment? = null, permissions: Array<String>) {
        if (fragment != null) {
            fragment.requestPermissions(permissions, 1)
        } else {
            ActivityCompat.requestPermissions(activity, permissions, 1)
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray, activity: androidx.fragment.app.FragmentActivity) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // All permissions granted, proceed with Bluetooth functionality
                startAlarmService()
            } else {
                // Handle permission denial (e.g., show a message, disable features, etc.)
                println("Permissions denied")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        Log.d(TAG, "onCreate called")

        checkBluetoothPermissionAndStartService(this)

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
                        viewModel = viewModel,
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

    private fun startProgress() {
        val tone = viewModel.tone.value
        val afterBlowDuration = viewModel.afterBlowDuration.value
        playTone(tone, afterBlowDuration)
        viewModel.setProgressPlaying(true)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d(TAG, "onKeyDown: keyCode = $keyCode")
        if (keyCode == KeyEvent.KEYCODE_CAMERA || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Log.d(TAG, "Key event matched, playing tone")
            startProgress()
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
    onButtonClick: () -> Unit,
    viewModel: MainViewModel
) {
    val progress by viewModel.progress.collectAsState()
    val isPlaying by viewModel.isProgressPlaying.collectAsState()
    val buttonText by viewModel.buttonText.collectAsState()
    val buttonColor by viewModel.buttonColor.collectAsState()

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            viewModel.startProgress()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "HEMA Tournament Buzzer")

        Spacer(modifier = Modifier.height(16.dp))
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            AfterBlowScreen(
                duration = afterBlowDuration,
                expanded = afterBlowExpanded,
                onDurationChanged = onDurationChanged,
                onExpandedChanged = onAfterBlowExpandedChanged,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            AlarmTonePicker(
                tone = tone,
                expanded = toneExpanded,
                onTypeChanged = onToneChanged,
                onExpandedChanged = onToneExpandedChanged,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                onButtonClick()
                viewModel.setProgressPlaying(true)
            },
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
        ) {
            Text(text = buttonText)
        }

        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
        )
    }
}

@Composable
fun AfterBlowScreen(
    duration: AfterBlowDuration,
    expanded: Boolean,
    onDurationChanged: (AfterBlowDuration) -> Unit,
    onExpandedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Text(
            text = "Afterblow: ${duration.duration}",
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
    onExpandedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
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
    val viewModel = MainViewModel()
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
            viewModel = viewModel,
            onButtonClick = {}
        )
    }
}