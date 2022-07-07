package work.racka.runningtracker.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import work.racka.runningtracker.data.database.Run
import work.racka.runningtracker.repository.MainRepository
import work.racka.runningtracker.util.SortType
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: MainRepository
): ViewModel() {

    private val _runs = MutableStateFlow<List<Run>>(listOf())
    val runs: StateFlow<List<Run>>
        get() = _runs

    private val _sort = MutableStateFlow(SortType.DATE)
    val sort: StateFlow<SortType>
        get() = _sort

    init {
        getRunFromDatabase()
    }

    fun insertRun(run: Run) {
        viewModelScope.launch {
            mainRepository.insertRun(run)
        }
    }

    private fun getRunFromDatabase() {
        viewModelScope.launch {
            when(_sort.value) {
                SortType.DATE -> mainRepository.getAllRunsSortedByDate()
                    .collect { _runs.emit(it) }
                SortType.RUNNING_TIME -> mainRepository.getAllRunsSortedByTimeInMillis()
                    .collect { _runs.emit(it) }
                SortType.AVG_SPEED -> mainRepository.getAllRunsSortedByAvgSpeed()
                    .collect { _runs.emit(it) }
                SortType.CALORIES_BURNED -> mainRepository.getAllRunsSortedByCaloriesBurned()
                    .collect { _runs.emit(it) }
                SortType.DISTANCE -> mainRepository.getAllRunsSortedByDistance()
                    .collect { _runs.emit(it) }
            }
        }
    }

    fun updateSort(sortType: SortType) {
        _sort.value = sortType
        getRunFromDatabase()
    }
}