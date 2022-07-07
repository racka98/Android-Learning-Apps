package work.racka.runningtracker.ui.views

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import org.w3c.dom.Text
import work.racka.runningtracker.R
import work.racka.runningtracker.data.database.Run
import work.racka.runningtracker.databinding.MarkerViewBinding
import work.racka.runningtracker.util.TrackingUtility
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView(
    private val runs: List<Run>,
    context: Context,
    layoutId: Int
) : MarkerView(context, layoutId) {


    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        val tvDate = this.findViewById<TextView>(R.id.tvDate)
        val tvAvgSpeed = this.findViewById<TextView>(R.id.tvAverageSpeed)
        val tvDistance = this.findViewById<TextView>(R.id.tvDistance)
        val tvDuration = this.findViewById<TextView>(R.id.tvDuration)
        val tvCaloriesBurned = this.findViewById<TextView>(R.id.tvCaloriesBurned)

        e ?: return
        val currentRunId = e.x.toInt()
        val run = runs[currentRunId]

        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        tvDate?.text = dateFormat.format(calendar.time)

        val avgSpeed = "${run.avgSpeedInKmh} KM/h"
        tvAvgSpeed?.text = avgSpeed

        val distanceInKm = "${run.distanceInM / 1000} KM"
        tvDistance?.text = distanceInKm

        tvDuration?.text = TrackingUtility
            .getFormattedStopWatchTime(run.timeInMillis)

        val caloriesBurned = "${run.caloriesBurned} kCal"
        tvCaloriesBurned?.text = caloriesBurned
    }
}