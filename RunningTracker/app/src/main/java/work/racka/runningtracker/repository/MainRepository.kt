package work.racka.runningtracker.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import work.racka.runningtracker.data.database.Run
import work.racka.runningtracker.data.database.RunDao
import work.racka.runningtracker.di.IoDispatcher
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val dao: RunDao,
    @IoDispatcher private val dbDispatcher: CoroutineDispatcher
) {

    suspend fun insertRun(run: Run) {
        withContext(dbDispatcher) {
            dao.insertRun(run)
        }
    }

    suspend fun deleteRun(run: Run) = dao.deleteRun(run)

    fun getAllRunsSortedByDate(): Flow<List<Run>> =
        dao.getAllRunsSortedByDate()

    fun getAllRunsSortedByDistance(): Flow<List<Run>> =
        dao.getAllRunsSortedByDistance()

    fun getAllRunsSortedByTimeInMillis(): Flow<List<Run>> =
        dao.getAllRunsSortedByTimeInMillis()

    fun getAllRunsSortedByAvgSpeed(): Flow<List<Run>> =
        dao.getAllRunsSortedByAvgSpeed()

    fun getAllRunsSortedByCaloriesBurned(): Flow<List<Run>> =
        dao.getAllRunsSortedByCaloriesBurned()


    fun getTotalAvgSpeed(): Flow<Float> =
        dao.getTotalAvgSpeed()

    fun getTotalTimeInMillis(): Flow<Long> =
        dao.getTotalTimeInMillis()

    fun getTotalCaloriesBurned(): Flow<Int> =
        dao.getTotalCaloriesBurned()

    fun getTotalDistance(): Flow<Int> =
        dao.getTotalDistance()
}