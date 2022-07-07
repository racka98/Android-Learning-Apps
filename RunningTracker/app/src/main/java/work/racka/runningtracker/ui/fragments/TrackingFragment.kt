package work.racka.runningtracker.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.UiSettings
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import work.racka.runningtracker.R
import work.racka.runningtracker.data.database.Run
import work.racka.runningtracker.databinding.FragmentTrackingBinding
import work.racka.runningtracker.service.TrackingService
import work.racka.runningtracker.ui.viewModel.MainViewModel
import work.racka.runningtracker.util.Constants.ACTION_PAUSE_SERVICE
import work.racka.runningtracker.util.Constants.ACTION_START_OR_RESUME_SERVICE
import work.racka.runningtracker.util.Constants.ACTION_STOP_SERVICE
import work.racka.runningtracker.util.Constants.MAP_ZOOM
import work.racka.runningtracker.util.Constants.POLYLINE_COLOR
import work.racka.runningtracker.util.Constants.POLYLINE_WIDTH
import work.racka.runningtracker.util.Polyline
import work.racka.runningtracker.util.TrackingUtility
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking),
    OnMapReadyCallback,
    OnMyLocationClickListener,
    OnMyLocationButtonClickListener {

    private lateinit var viewBinding: FragmentTrackingBinding

    private val viewModel: MainViewModel by viewModels()

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var currentTimeMillis = 0L

    @set:Inject
    var weight: Float = 1f

    private var map: GoogleMap? = null
    private lateinit var menu: Menu

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding = FragmentTrackingBinding.bind(view)

        val mapView = viewBinding.mapView

        viewBinding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        viewBinding.btnFinishRun.setOnClickListener {
            zoomOutMap()
            endRunAndSaveToDb()
        }

        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync(this)

        subscribeToFlow()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (currentTimeMillis > 0L) {
            this.menu.getItem(0).isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miCancelTracking -> {
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel the Run")
            .setMessage("Are you sure to cancel to cancel and delete it's data?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") { _, _ ->
                stopRun()
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
            .show()
    }

    private fun stopRun() {
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private fun subscribeToFlow() {
        lifecycleScope.launchWhenStarted {
            TrackingService.isTracking.collect {
                Timber.d("IsTracking: $it")
                updateTracking(it)
            }
        }

        // Needs to be replaced with StateFlow
        TrackingService.pathPoints.observe(viewLifecycleOwner) {
            Timber.d("PathPoints updated: $it")
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        }

        lifecycleScope.launchWhenStarted {
            TrackingService.timeRunInMillis.collect {
                currentTimeMillis = it
                val formattedTime = TrackingUtility
                    .getFormattedStopWatchTime(currentTimeMillis)
                viewBinding.tvTimer.text = formattedTime
            }
        }
    }

    private fun toggleRun() {
        if (isTracking) {
            menu.getItem(0).isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        val btnToggleRun = viewBinding.btnToggleRun
        this.isTracking = isTracking
        if (!isTracking) {
            btnToggleRun.apply {
                isEnabled = false
                text = "Start"
                isEnabled = true
            }
            viewBinding.btnFinishRun.visibility = View.VISIBLE
        } else {
            btnToggleRun.apply {
                isEnabled = false
                text = "Stop"
                isEnabled = true
            }
            menu.getItem(0).isVisible = true
            viewBinding.btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory
                    .newLatLngZoom(
                        pathPoints.last().last(),
                        MAP_ZOOM
                    )
            )
        }
    }

    private fun zoomOutMap() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (position in polyline) {
                bounds.include(position)
            }
        }

        val mapView = viewBinding.mapView
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDb() {
        map?.snapshot { btmp ->
            var distanceInMeters = 0
            for (polyline in pathPoints) {
                distanceInMeters += TrackingUtility
                    .calculatePolylineLength(polyline)
                    .toInt()
            }
            val speedBeforeRound =
                (distanceInMeters / 1000f) / TimeUnit.MILLISECONDS.toHours(currentTimeMillis)
            val avgSpeed = round(speedBeforeRound * 10) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val run = Run(
                img = btmp,
                timestamp = dateTimeStamp,
                avgSpeedInKmh = avgSpeed,
                distanceInM = distanceInMeters,
                timeInMillis = currentTimeMillis,
                caloriesBurned = caloriesBurned
            )
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run Save Successfully!",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().lastIndex - 1]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    override fun onResume() {
        super.onResume()
        viewBinding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewBinding.mapView.onPause()
    }

    override fun onStart() {
        super.onStart()
        viewBinding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        viewBinding.mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        viewBinding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewBinding.mapView.onSaveInstanceState(outState)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        this.map = map
        addAllPolylines()
        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true
        map.isTrafficEnabled = true
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(requireContext(), "Current location:\n$location", Toast.LENGTH_LONG)
            .show()
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(requireContext(), "MyLocation button clicked", Toast.LENGTH_SHORT)
            .show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }
}