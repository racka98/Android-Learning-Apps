package com.example.android.guesstheword.screens.score

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ScoreViewModel(finalScore: Int): ViewModel() {

    private val _score = MutableLiveData<Int>()
    val score: LiveData<Int> = _score

    private val _eventPlayAgain = MutableLiveData<Boolean>()
    val eventPlayAgain: LiveData<Boolean> = _eventPlayAgain

    init {
        _score.value = finalScore
        _eventPlayAgain.value = false
    }

    fun onPlayAgain() {
        _eventPlayAgain.value = true
    }

    fun onPlayAgainCompleted() {
        _eventPlayAgain.value = false
    }
}