package work.racka.runningtracker.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import work.racka.runningtracker.util.Constants
import work.racka.runningtracker.util.Constants.RUNNING_TABLE

@Dao
interface RunDao {

    @Insert(onConflict = REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query(Constants.QUERY_GET_ALL_RUNS)
    fun getAllRunsSortedByDate(): Flow<List<Run>>

    @Query("SELECT * FROM $RUNNING_TABLE ORDER BY timeInMillis DESC")
    fun getAllRunsSortedByTimeInMillis(): Flow<List<Run>>

    @Query("SELECT * FROM $RUNNING_TABLE ORDER BY caloriesBurned DESC")
    fun getAllRunsSortedByCaloriesBurned(): Flow<List<Run>>

    @Query("SELECT * FROM $RUNNING_TABLE ORDER BY distanceInM DESC")
    fun getAllRunsSortedByDistance(): Flow<List<Run>>

    @Query("SELECT * FROM $RUNNING_TABLE ORDER BY avgSpeedInKmh DESC")
    fun getAllRunsSortedByAvgSpeed(): Flow<List<Run>>


    @Query("SELECT SUM(timeInMillis) FROM $RUNNING_TABLE")
    fun getTotalTimeInMillis(): Flow<Long>

    @Query("SELECT SUM(caloriesBurned) FROM $RUNNING_TABLE")
    fun getTotalCaloriesBurned(): Flow<Int>

    @Query("SELECT SUM(distanceInM) FROM $RUNNING_TABLE")
    fun getTotalDistance(): Flow<Int>

    @Query("SELECT AVG(avgSpeedInKmh) FROM $RUNNING_TABLE")
    fun getTotalAvgSpeed(): Flow<Float>

}