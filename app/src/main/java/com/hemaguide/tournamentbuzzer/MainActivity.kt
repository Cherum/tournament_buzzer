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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.hemaguide.tournamentbuzzer.ui.theme.TournamentAlarmTheme

class MainActivity : ComponentActivity() {
    private var tone = ToneType.FIRST
    private var afterBlowDuration = AfterBlowDuration.NONE

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
        Log.d("MainActivity", "onCreate called")

        checkBluetoothPermissionAndStartService(this)

        setContent {
            TournamentAlarmTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        onToneChanged = {tone = it},
                        onDurationChanged = {afterBlowDuration = it},
                        onButtonClick = { playTone(tone, afterBlowDuration) }
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
            playTone(tone, afterBlowDuration)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onDurationChanged: (AfterBlowDuration) -> Unit,
    onToneChanged: (ToneType) -> Unit,
    onButtonClick: () -> Unit
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

        Spacer(modifier = Modifier.height(16.dp))
        AfterBlowScreen (onDurationChanged = {onDurationChanged(it)})

        Spacer(modifier = Modifier.height(16.dp))
        AlarmTonePicker (onTypeChanged = {onToneChanged(it)})

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onButtonClick() }) {
            Text(text = "Play Sound")
        }
    }
}

@Composable
fun AfterBlowScreen(onDurationChanged: (AfterBlowDuration) -> Unit){
    var afterBlowDuration by rememberSaveable { mutableStateOf(AfterBlowDuration.NONE) }
    var afterBlowExpanded by remember { mutableStateOf(false) }
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
            text = "Afterblow duration: ${afterBlowDuration.duration}",
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
                        afterBlowExpanded = false
                        afterBlowDuration = afterblowDuration
                        onDurationChanged(afterblowDuration)
                    },
                    text = { Text(text = afterblowDuration.duration) }
                )
            }
        }
    }
}

@Composable
fun AlarmTonePicker(onTypeChanged: (ToneType) -> Unit){
    var selectedTone by rememberSaveable { mutableStateOf(ToneType.FIRST) }
    var toneExpanded by remember { mutableStateOf(false) }
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
                        toneExpanded = false
                        selectedTone = tone
                        onTypeChanged(tone)
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
        MainScreen(onButtonClick = {}, onDurationChanged = {}, onToneChanged = {})
    }
}
