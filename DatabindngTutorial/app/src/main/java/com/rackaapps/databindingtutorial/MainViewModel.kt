package com.rackaapps.databindingtutorial

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    val currentRandomFruitName: LiveData<String> = FakeRepository.currentRandomFruitName
    fun onChangeRandomFruitClick() = FakeRepository.changeCurrentRandomFruitName()


    val editTextContent = MutableLiveData<String>()

    private val _displayedEditTextContent = MutableLiveData<String>()
    val displayedEditTextContent: LiveData<String> = _displayedEditTextContent
    fun onDisplayEditTextContentClick() {
        _displayedEditTextContent.value = editTextContent.value
    }

    fun onSelectRandomEditTextFruit() {
        editTextContent.value = FakeRepository.getRandomFruitName()
    }
}