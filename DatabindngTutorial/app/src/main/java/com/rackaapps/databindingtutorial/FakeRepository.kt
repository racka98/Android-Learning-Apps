package com.rackaapps.databindingtutorial

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object FakeRepository {
    private val fruitNames = listOf(
        "Apple", "Banana", "Mango", "Orange", "PawPaw",
        "Kiwi", "Pear", "Strawberry", "Raspberry", "Grape"
    )
    private val _currentRandomFruitName = MutableLiveData<String>()
    val currentRandomFruitName: LiveData<String> = _currentRandomFruitName
    init {
        _currentRandomFruitName.value = fruitNames.first()
    }
    fun getRandomFruitName(): String {
        return fruitNames.random()
    }
    fun changeCurrentRandomFruitName() {
        _currentRandomFruitName.value = getRandomFruitName()
    }
}