/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 * This contains all the steps for adding coroutines
 * Each step is labelled
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    //1. Defining the Job in viewModel
    //Always remember to cancel the job in onCleared
    //private var viewModelJob = Job()

    //2. Cancelling the job when done with onCleared()
    //Don't need to cancel job with viewModelScope
//    override fun onCleared() {
//        super.onCleared()
//        viewModelJob.cancel()
//    }

    //3. Defining the uiScope for coroutines running in the main thread
    //Update: Use viewModelScope instead
    //private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    //4. Defining the tonight variable that holds the current night's data as MutableLiveData
    private var tonight = MutableLiveData<SleepNight?>()

    //5. Defining a variables for all nights present in the database
    val nights = database.getAllNights()

    //14. Formatting the nights data into string via formatNights in Util.kt
    val nightsString = Transformations.map(nights) { nights ->
        formatNights(nights, application.resources)
    }

    //Live data for navigating to sleepQuality fragment
    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
    val navigateToSleepQuality: LiveData<SleepNight> = _navigateToSleepQuality

    /** Set the visibility of the button
     * The START button should be visible when tonight is null,
     * The STOP button when tonight is not null, and
     * The CLEAR button if nights contains any nights
     */
//    val startButtonVisible = Transformations.map(tonight) {
//        it == null
//    }
//    val stopButtonVisible = Transformations.map(tonight) {
//        it != null
//    }
    private val _startButtonVisible = MutableLiveData<Boolean>()
    val startButtonVisible: LiveData<Boolean> = _startButtonVisible

    private val _stopButtonVisible = MutableLiveData<Boolean>()
    val stopButtonVisible: LiveData<Boolean> = _stopButtonVisible

    //Live data for navigating to sleepDetail Fragment
    private val _navigateToSleepDataQuality = MutableLiveData<Long>()
    val navigateToSleepDataQuality
        get() = _navigateToSleepDataQuality

    val clearButtonVisible = Transformations.map(nights) {
        it?.isNotEmpty()
    }

    //Live data for showing snackbar
    private val _showSnackbarEvent = MutableLiveData<Boolean>()
    val showSnackbarEvent: LiveData<Boolean> = _showSnackbarEvent
    //Reset showSnackbar to false
    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
    }

    //init block to initialize tonight
    init {
        initializeTonight()
        _startButtonVisible.value = true
        _stopButtonVisible.value = false
    }

    //6. Defining a function to initialize tonight in uiScope and launch a coroutine
    private fun initializeTonight() {
        viewModelScope.launch {
            //Call the getTonightFromDatabase function to set a value for tonight
            tonight.value = getTonightFromDatabase()
        }
    }

    //7. Implement getTonightFromDatabase() for initializeTonight()
    //Mark it as suspend so that it runs inside the Coroutine and does not block
    private suspend fun getTonightFromDatabase(): SleepNight? {
        var night = database.getTonight()
        if (night?.endTimeMilli != night?.startTimeMilli) {
            night = null
        }
        return night
//        //return result of coroutine running in Dispatchers.IO context
//        return withContext(Dispatchers.IO) {
//            var night = database.getTonight()
//            if (night?.endTimeMilli != night?.startTimeMilli) {
//                night = null
//            }
//            night
//        }
    }

    //8. Defining onStartTracking() which will be the click handler for the Start button
    fun onStartTracking() {
        _startButtonVisible.value = false
        _stopButtonVisible.value = true
        viewModelScope.launch {
            //Creating new sleepNight which captures current time as start time
            val newNight = SleepNight()
            //Call the insert() function to insert data into the database
            insert(newNight)
        }
    }

    //9. Implement insert() function for inputting data into database
    //Mark it as suspend so that it runs inside the Coroutine and does not block
    private suspend fun insert(night: SleepNight) {
        database.insert(night)
//        withContext(Dispatchers.IO) {
//            database.insert(night)
//        }
    }

    //10. Defining onStopTracking() which will be the click handler for the stop button
    fun onStopTracking() {
        _startButtonVisible.value = true
        _stopButtonVisible.value = false
        viewModelScope.launch {
            //Assign the oldNight value on button press
            // In Kotlin, the return@label syntax is used for specifying which function among
            // several nested ones this statement returns from.
            // In this case, we are specifying to return from launch(),
            // not the lambda.
            val oldNight = getTonightFromDatabase() ?: return@launch
            //Assign the end time of the night as the current system time when pressing the button
            oldNight.endTimeMilli = System.currentTimeMillis()
            //Call update() function to update the database when the stop button is clicked
            update(oldNight)
            //Set the value of navigateToSleepQuality to oldNight so that it can be observed in the fragment
            _navigateToSleepQuality.value = oldNight
        }
    }

    //11. Implement update() function for updating data in the database
    private suspend fun update(night: SleepNight) {
//        withContext(Dispatchers.IO) {
//            database.update(night)
//        }
        database.update(night)
    }

    //12. Defining onClear() which will be click handler for the clear button
    fun onClear() {
        viewModelScope.launch {
            //Call clearAll() function to clear SleepNight values in database
            clearAll()
            tonight.value = null
            _showSnackbarEvent.value = true
        }
    }

    //13. Implement clearAll() function for clearing data from the database
    private suspend fun clearAll() {
        database.clearAll()
//        withContext(Dispatchers.IO) {
//            database.clearAll()
//        }
    }

    //Done navigation function to reset the navigateToSleepQuality value to null
    fun doneNavigating() {
        _navigateToSleepQuality.value = null
    }

    //Click handlers connected to button via Databinding in the XML files

    //Method for navigation on clicking
    fun onSleepNightClicked(id: Long) {
        _navigateToSleepDataQuality.value = id
    }

    fun onSleepDataQualityNavigated() {
        _navigateToSleepDataQuality.value = null
    }
}

