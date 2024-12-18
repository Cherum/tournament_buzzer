package com.hemaguide.tournamentbuzzer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _tone = MutableStateFlow(ToneType.FIRST)
    val tone: StateFlow<ToneType> = _tone.asStateFlow()

    private val _afterBlowDuration = MutableStateFlow(AfterBlowDuration.NONE)
    val afterBlowDuration: StateFlow<AfterBlowDuration> = _afterBlowDuration.asStateFlow()

    fun setTone(toneType: ToneType) {
        viewModelScope.launch {
            _tone.emit(toneType)
        }
    }

    fun setAfterBlowDuration(duration: AfterBlowDuration) {
        viewModelScope.launch {
            _afterBlowDuration.emit(duration)
        }
    }
}