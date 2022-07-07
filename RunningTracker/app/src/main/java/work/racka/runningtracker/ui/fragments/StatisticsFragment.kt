package work.racka.runningtracker.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import work.racka.runningtracker.R
import work.racka.runningtracker.databinding.FragmentStatisticsBinding
import work.racka.runningtracker.ui.state.StatisticsState
import work.racka.runningtracker.ui.viewModel.StatisticsViewModel
import work.racka.runningtracker.ui.views.CustomMarkerView
import work.racka.runningtracker.util.ThemeColor.themeColor
import work.racka.runningtracker.util.TrackingUtility
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private val viewModel: StatisticsViewModel by viewModels()

    private lateinit var binding: FragmentStatisticsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStatisticsBinding.bind(view)

        subscribeToFlow()
        setUpBarChart()
    }

    private fun setUpBarChart() {
        val barChart: BarChart = binding.barChart
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = requireContext().themeColor(android.R.attr.textColorSecondary)
            textColor = requireContext().themeColor(android.R.attr.textColorSecondary)
            setDrawGridLines(false)
        }
        barChart.axisLeft.apply {
            axisLineColor = requireContext().themeColor(android.R.attr.textColorSecondary)
            textColor = requireContext().themeColor(android.R.attr.textColorSecondary)
            setDrawGridLines(false)
        }
        barChart.axisRight.apply {
            axisLineColor = requireContext().themeColor(android.R.attr.textColorSecondary)
            textColor = requireContext().themeColor(android.R.attr.textColorSecondary)
            setDrawGridLines(false)
        }
        barChart.apply {
            description.text = "Average Speed Over Time"
            legend.isEnabled = false
        }
    }

    private fun subscribeToFlow() {
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { state ->
                handleState(state)
            }
        }
    }

    private fun handleState(state: StatisticsState) {
        if (state is StatisticsState.Statistics) {
            val totalTimeRun = TrackingUtility
                .getFormattedStopWatchTime(state.totalTimeRun)
            binding.tvTotalTime.text = totalTimeRun

            val totalDistanceInKm = round(10f * state.totalDistance / 1000f) / 10f
            val totalDistanceInKmText = "${totalDistanceInKm}km"
            binding.tvTotalDistance.text = totalDistanceInKmText

            val avgSpeed = round(10f * state.totalAvgSpeed) / 10f
            val avgSpeedText = "${avgSpeed}km/h"
            binding.tvAverageSpeed.text = avgSpeedText

            val totalCaloriesBurned = "${state.totalCaloriesBurned}kCal"
            binding.tvTotalCalories.text = totalCaloriesBurned

            val barChart = binding.barChart
            val runDistances = state.runsSortedByDate.indices.map { index ->
                BarEntry(index.toFloat(), state.runsSortedByDate[index].distanceInM.toFloat())
            }
            val barDataSet = BarDataSet(runDistances, "Run Distance Over Time").apply {
                valueTextColor = requireContext().themeColor(android.R.attr.textColorSecondary)
                color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
            }
            barChart.data = BarData(barDataSet)
            barChart.marker = CustomMarkerView(
                state.runsSortedByDate.reversed(),
                requireContext(),
                R.layout.marker_view
            )
            barChart.invalidate()
        }
    }
}