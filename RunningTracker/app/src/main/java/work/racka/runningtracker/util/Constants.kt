package work.racka.runningtracker.util

import android.graphics.Color
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_MIN

object Constants {
    const val RUNNING_TABLE = "running_table"
    const val QUERY_GET_ALL_RUNS = "SELECT * FROM $RUNNING_TABLE ORDER BY timestamp DESC"
    const val SETTINGS_PREFERENCES = "settings_preference"
    const val REQUEST_CODE_LOCATION_PERMISSIONS = 0

    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"

    const val TIMER_UPDATE_INTERVAL = 50L

    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_UPDATE_INTERVAL = 2000L

    const val POLYLINE_COLOR = Color.GREEN
    const val POLYLINE_WIDTH = 8f
    const val MAP_ZOOM =  15f

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1

    const val SHARED_PREFERENCES_NAME = "sharedPref"
    const val KEY_FIRST_TIME_TOGGLE = "KEY_FIRST_TIME_TOGGLE"
    const val KEY_NAME = "KEY_NAME"
    const val KEY_WEIGHT = "KEY_WEIGHT"
}