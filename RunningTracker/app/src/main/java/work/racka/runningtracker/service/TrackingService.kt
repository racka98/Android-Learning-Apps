package work.racka.runningtracker.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import work.racka.runningtracker.R
import work.racka.runningtracker.util.Constants.ACTION_PAUSE_SERVICE
import work.racka.runningtracker.util.Constants.ACTION_START_OR_RESUME_SERVICE
import work.racka.runningtracker.util.Constants.ACTION_STOP_SERVICE
import work.racka.runningtracker.util.Constants.FASTEST_UPDATE_INTERVAL
import work.racka.runningtracker.util.Constants.LOCATION_UPDATE_INTERVAL
import work.racka.runningtracker.util.Constants.NOTIFICATION_CHANNEL_ID
import work.racka.runningtracker.util.Constants.NOTIFICATION_CHANNEL_NAME
import work.racka.runningtracker.util.Constants.NOTIFICATION_ID
import work.racka.runningtracker.util.Constants.TIMER_UPDATE_INTERVAL
import work.racka.runningtracker.util.Polylines
import work.racka.runningtracker.util.TrackingUtility
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    private var isFirstRun = true
    private var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    private lateinit var currentNotificationBuilder: NotificationCompat.Builder

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

    private lateinit var notificationManager: NotificationManager

    companion object {
        val timeRunInMillis = MutableStateFlow(0L)
        val isTracking = MutableStateFlow(false)

        // Needs to be replaced with StateFlow
        val pathPoints = MutableLiveData<Polylines>(mutableListOf())
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager
        currentNotificationBuilder = notificationBuilder
        lifecycleScope.launchWhenStarted {
            isTracking.collect {
                updateLocationTracking(it)
                updateNotificationTrackingState(it)
            }
        }

        lifecycleScope.launchWhenStarted {
            timeRunInMillis.collect {
                if (!serviceKilled) {
                    val notification = notificationBuilder
                        .setContentText(
                            TrackingUtility
                                .getFormattedStopWatchTime(it, false)
                        )
                    notificationManager.notify(NOTIFICATION_ID, notification.build())
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        lifecycleScope.launchWhenStarted {
                            startForegroundService()
                        }
                        isFirstRun = false
                    } else {
                        lifecycleScope.launchWhenStarted {
                            startTimer()
                        }
                        Timber.d("Resuming Service")
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    lifecycleScope.launchWhenStarted {
                        pauseService()
                    }
                    Timber.d("Service paused")
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Service stopped")
                    lifecycleScope.launchWhenStarted {
                        killService()
                    }
                }
                else -> {
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private suspend fun killService() {
        serviceKilled = true
        isFirstRun = true
        isTracking.emit(false)
        timeRunInMillis.emit(0L)
        pauseService()
        stopForeground(true)
        stopSelf()
    }

    private suspend fun startTimer() {
        addEmptyPolyline()
        isTracking.emit(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true

        while (isTracking.value) {
            lapTime = System.currentTimeMillis() - timeStarted
            timeRunInMillis.emit(timeRun + lapTime)
            if (timeRunInMillis.value >= lastSecondTimeStamp + 1000L) {
                //timeRunInSeconds.emit(timeRunInSeconds.value + 1)
                lastSecondTimeStamp += 1000L
            }
            delay(TIMER_UPDATE_INTERVAL)
        }

        timeRun += lapTime
    }

    private suspend fun pauseService() {
        isTracking.emit(false)
        isTimerEnabled = false
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(
                this,
                1,
                pauseIntent,
                FLAG_IMMUTABLE
            )
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(
                this,
                2,
                resumeIntent,
                FLAG_IMMUTABLE
            )
        }

        notificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(notificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if (!serviceKilled) {
            val currentNotificationBuilder = notificationBuilder
                .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking &&
            TrackingUtility.hasLocationPermissions(this)
        ) {
            val request = LocationRequest().apply {
                interval = LOCATION_UPDATE_INTERVAL
                fastestInterval = FASTEST_UPDATE_INTERVAL
                priority = PRIORITY_HIGH_ACCURACY
            }
            fusedLocationProviderClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value) {
                result.locations.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("New Location: ${location.latitude}, ${location.longitude}")
                        Timber.d("New PathPoints : ${pathPoints.value}")
                    }
                }
            }
        }

    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    }

    private suspend fun startForegroundService() {
        startTimer()
        //isTracking.emit(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}