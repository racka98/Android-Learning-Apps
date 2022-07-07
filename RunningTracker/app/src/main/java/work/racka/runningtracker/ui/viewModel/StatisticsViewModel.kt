package work.racka.runningtracker.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import work.racka.runningtracker.repository.MainRepository
import work.racka.runningtracker.ui.state.StatisticsState
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val mainRepository: MainRepository
): ViewModel() {

    private val totalTimeRun = mainRepository.getTotalTimeInMillis()
    private val totalDistance = mainRepository.getTotalDistance()
    private val totalCaloriesBurned = mainRepository.getTotalCaloriesBurned()
    private val totalAvgSpeed = mainRepository.getTotalAvgSpeed()
    private val runsSortedByDate = mainRepository.getAllRunsSortedByDate()

    val uiState: StateFlow<StatisticsState> = combine(
        totalTimeRun,
        totalDistance,
        totalCaloriesBurned,
        totalAvgSpeed,
        runsSortedByDate
    ) { totalTimeRun, totalDistance, totalCaloriesBurned,
        totalAvgSpeed, runsSortedByDate ->
        StatisticsState.Statistics(
            totalTimeRun = totalTimeRun,
            totalDistance = totalDistance,
            totalAvgSpeed = totalAvgSpeed,
            totalCaloriesBurned = totalCaloriesBurned,
            runsSortedByDate = runsSortedByDate
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatisticsState.Empty
    )
}