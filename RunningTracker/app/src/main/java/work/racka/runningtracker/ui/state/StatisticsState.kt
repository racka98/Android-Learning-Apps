package work.racka.runningtracker.ui.state

import work.racka.runningtracker.data.database.Run

sealed class StatisticsState {
    data class Statistics(
        val totalTimeRun: Long = 0L,
        val totalAvgSpeed: Float = 0f,
        val totalCaloriesBurned: Int = 0,
        val totalDistance: Int = 0,
        val runsSortedByDate: List<Run> = listOf()
    ) : StatisticsState()

    companion object {
        val Empty = Statistics()
    }
}
