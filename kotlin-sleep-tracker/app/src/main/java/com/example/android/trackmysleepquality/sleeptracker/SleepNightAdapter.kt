package com.example.android.trackmysleepquality.sleeptracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.databinding.ListItemSleepNightBinding


class SleepNightAdapter(private val clickListener: SleepNightListener): ListAdapter<SleepNight, SleepNightAdapter.ViewHolder>(SleepNightDiffCallback()) {

    //These are no longer needed with ListAdapter being used in place of RecyclerView.Adapter
//    var data = listOf<SleepNight>()
//        set(value) {
//            field = value
//            notifyDataSetChanged()
//        }
//
//    override fun getItemCount(): Int {
//        return data.size
//    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val item = data[position]
        val item = getItem(position)
        holder.bind(item, clickListener)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    //ViewHolder for our list_item_sleep_night.xml to be used in the adapter
    class ViewHolder private constructor(val binding: ListItemSleepNightBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SleepNight, clickListener: SleepNightListener) {
            //Switching to data binding with BindingAdapter
//            val res: Resources = itemView.context.resources
//            binding.sleepLength.text = convertDurationToFormatted(item.startTimeMilli, item.endTimeMilli, res)
//            binding.qualityString.text = convertNumericQualityToString(item.sleepQuality, res)
//            binding.qualityImage.setImageResource(
//                when (item.sleepQuality) {
//                    0 -> R.drawable.ic_sleep_0
//                    1 -> R.drawable.ic_sleep_1
//                    2 -> R.drawable.ic_sleep_2
//                    3 -> R.drawable.ic_sleep_3
//                    4 -> R.drawable.ic_sleep_4
//                    5 -> R.drawable.ic_sleep_5
//                    else -> R.drawable.ic_sleep_active
//                }
//            )

            //Data binding with BindingAdapter
            binding.sleepNight = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
            //Add the required BindingAdapter to the respective views inside list_item_sleep_night.xml
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemSleepNightBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    class SleepNightDiffCallback: DiffUtil.ItemCallback<SleepNight>() {
        override fun areItemsTheSame(oldItem: SleepNight, newItem: SleepNight): Boolean {
            return oldItem.nightId == newItem.nightId
        }

        override fun areContentsTheSame(oldItem: SleepNight, newItem: SleepNight): Boolean {
            return oldItem == newItem
        }

    }
}

class SleepNightListener(val clickListener: (sleepId: Long) -> Unit) {
    fun onClick(night: SleepNight) = clickListener(night.nightId)
}