package work.racka.runningtracker.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import work.racka.runningtracker.data.database.Run
import work.racka.runningtracker.databinding.ItemRunBinding
import work.racka.runningtracker.util.TrackingUtility
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter : ListAdapter<Run, RunAdapter.RunViewHolder>(DiffCallback) {

    class RunViewHolder(val binding: ItemRunBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): RunViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemRunBinding.inflate(layoutInflater, parent, false)
                return RunViewHolder(binding)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder.from(parent)
    }

    override fun onBindViewHolder(holderRun: RunViewHolder, position: Int) {
        val run = getItem(position)
        holderRun.itemView.apply {
            Glide.with(this)
                .load(run.img)
                .into(holderRun.binding.ivRunImage)

            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            holderRun.binding.tvDate.text = dateFormat.format(calendar.time)

            val avgSpeed = "${run.avgSpeedInKmh} KM/h"
            holderRun.binding.tvAvgSpeed.text = avgSpeed

            val distanceInKm = "${run.distanceInM / 1000} KM"
            holderRun.binding.tvDistance.text = distanceInKm

            holderRun.binding.tvTime.text = TrackingUtility
                .getFormattedStopWatchTime(run.timeInMillis)

            val caloriesBurned = "${run.caloriesBurned} kCal"
            holderRun.binding.tvCalories.text = caloriesBurned
        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }
}