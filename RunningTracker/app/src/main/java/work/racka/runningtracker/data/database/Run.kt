package work.racka.runningtracker.data.database

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import work.racka.runningtracker.util.Constants

@Entity(tableName = Constants.RUNNING_TABLE)
data class Run(
    var img: Bitmap? = null,
    var timestamp: Long = 0L,
    var avgSpeedInKmh: Float = 0f,
    var distanceInM: Int = 0,
    var timeInMillis: Long = 0L,
    var caloriesBurned: Int = 0
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}