package com.hemaguide.tournamentbuzzer

enum class AfterBlowDuration (var duration: String){
    NONE("None"),
    ZERO_ONE("0.1 s"),
    ZERO_TWO("0.2 s"),
    ZERO_THREE("0.3 s"),
    ZERO_FOUR("0.4 s"),
    ZERO_FIVE("0.5 s"),
    ZERO_SIX("0.6 s"),
    ZERO_SEVEN("0.7 s"),
    ZERO_EIGHT("0.8 s"),
    ZERO_NINE("0.9 s"),
    ONE_SECOND("1 s")
}

val AFTER_BLOW_OPTIONS = listOf(
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