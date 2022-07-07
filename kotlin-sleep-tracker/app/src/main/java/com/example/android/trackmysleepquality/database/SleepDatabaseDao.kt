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

package com.example.android.trackmysleepquality.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface SleepDatabaseDao {

    //Inserting data into the table
    @Insert
    suspend fun insert(night: SleepNight)

    //Updating data in the table
    @Update
    suspend fun update(night: SleepNight)

    //Selecting all data from the table where the nightId equals the provided key in the function
    @Query("SELECT * FROM daily_sleep_quality_table WHERE nightId = :key")
    suspend fun get(key: Long): SleepNight?

    //Delete all rows from the database
    @Query("DELETE FROM daily_sleep_quality_table")
    suspend fun clearAll()

    //Selecting all nights in the database and mapping them to LiveData
    @Query("SELECT * FROM daily_sleep_quality_table ORDER BY nightId DESC")
    fun getAllNights(): LiveData<List<SleepNight>>

    //Selecting one night
    @Query("SELECT * FROM daily_sleep_quality_table ORDER BY nightId DESC LIMIT 1")
    suspend fun getTonight(): SleepNight?

    /**
     * Selects and returns the night with given nightId.
     */
    @Query("SELECT * from daily_sleep_quality_table WHERE nightId = :key")
    fun getNightWithId(key: Long): LiveData<SleepNight>
}
