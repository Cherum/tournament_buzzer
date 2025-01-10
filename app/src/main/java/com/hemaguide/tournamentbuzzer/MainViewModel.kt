package com.hemaguide.tournamentbuzzer

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _tone = MutableStateFlow(ToneType.FIRST)
    val tone: StateFlow<ToneType> = _tone.asStateFlow()
    fun setTone(toneType: ToneType) {
        viewModelScope.launch {
            _tone.emit(toneType)
        }
    }

    private val _afterBlowDuration = MutableStateFlow(AfterBlowDuration.ZERO_FIVE)
    val afterBlowDuration: StateFlow<AfterBlowDuration> = _afterBlowDuration.asStateFlow()
    fun setAfterBlowDuration(duration: AfterBlowDuration) {
        viewModelScope.launch {
            _afterBlowDuration.emit(duration)
        }
    }

    private val _afterBlowExpanded = MutableStateFlow(false)
    val afterBlowExpanded: StateFlow<Boolean> = _afterBlowExpanded.asStateFlow()
    fun setAfterBlowExpanded(expanded: Boolean) {
        viewModelScope.launch {
            _afterBlowExpanded.emit(expanded)
        }
    }

    private val _toneExpanded = MutableStateFlow(false)
    val toneExpanded: StateFlow<Boolean> = _toneExpanded.asStateFlow()
    fun setToneExpanded(expanded: Boolean) {
        viewModelScope.launch {
            _toneExpanded.emit(expanded)
        }
    }


    private val _progress = MutableStateFlow(0.0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _isProgressPlaying = MutableStateFlow(false)
    val isProgressPlaying: StateFlow<Boolean> = _isProgressPlaying.asStateFlow()

    private val _buttonText = MutableStateFlow("Play Sound")
    val buttonText: StateFlow<String> = _buttonText.asStateFlow()

    private val _buttonColor = MutableStateFlow(Color.Blue)
    val buttonColor: StateFlow<Color> = _buttonColor.asStateFlow()

    fun setProgressPlaying(isPlaying: Boolean) {
        _isProgressPlaying.value = isPlaying
        if (isPlaying) {
            _buttonText.value = "Playing..."
            _buttonColor.value = Color.Red
            startProgress()
        } else {
            _buttonText.value = "Play Sound"
            _buttonColor.value = Color.Blue
        }
    }

    fun startProgress() {
        viewModelScope.launch {
            val totalDuration = afterBlowDuration.value.durationInMillis
            val interval = 100L
            for (time in 0..totalDuration step interval.toInt()) {
                _progress.value = time / totalDuration.toFloat()
                delay(interval)
            }
            _progress.value = 1.0f
            _isProgressPlaying.value = false
            setProgressPlaying(false)
        }
    }

}