package work.racka.runningtracker.ui.fragments

import android.Manifest
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import work.racka.runningtracker.R
import work.racka.runningtracker.databinding.FragmentRunBinding
import work.racka.runningtracker.ui.adapters.RunAdapter
import work.racka.runningtracker.ui.viewModel.MainViewModel
import work.racka.runningtracker.util.Constants.KEY_NAME
import work.racka.runningtracker.util.Constants.REQUEST_CODE_LOCATION_PERMISSIONS
import work.racka.runningtracker.util.SortType
import work.racka.runningtracker.util.TrackingUtility
import javax.inject.Inject

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks {

    private lateinit var viewBinding: FragmentRunBinding
    private val viewModel: MainViewModel by viewModels()

    private lateinit var runAdapter: RunAdapter

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @set:Inject
    var userName: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding = FragmentRunBinding.bind(view)

        setToolbarTitleText()
        setUpRecyclerView()
        requestLocationPermissions()
        viewBinding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }

        val spinnerFilter = viewBinding.spFilter
        lifecycleScope.launchWhenStarted {
            viewModel.sort.collect {
                when(it) {
                    SortType.DATE -> spinnerFilter.setSelection(0)
                    SortType.RUNNING_TIME -> spinnerFilter.setSelection(1)
                    SortType.DISTANCE -> spinnerFilter.setSelection(2)
                    SortType.AVG_SPEED -> spinnerFilter.setSelection(3)
                    SortType.CALORIES_BURNED -> spinnerFilter.setSelection(4)
                }
            }
        }

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                when(pos) {
                    0 -> viewModel.updateSort(SortType.DATE)
                    1 -> viewModel.updateSort(SortType.RUNNING_TIME)
                    2 -> viewModel.updateSort(SortType.DISTANCE)
                    3 -> viewModel.updateSort(SortType.AVG_SPEED)
                    4 -> viewModel.updateSort(SortType.CALORIES_BURNED)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) { }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.runs.collect {
                runAdapter.submitList(it)
            }
        }
    }

    private fun setUpRecyclerView() = viewBinding.rvRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
    }

    private fun setToolbarTitleText() {
        val name = sharedPreferences.getString(KEY_NAME, "")
        val toolbarText = "Let's Go, $name!"
        val toolbarTitle = requireActivity().findViewById<MaterialTextView>(R.id.tvToolbarTitle)
        toolbarTitle.text = toolbarText
    }

    private fun requestLocationPermissions() {
        if (!TrackingUtility.hasLocationPermissions(requireContext())) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                EasyPermissions.requestPermissions(
                    this,
                    "Location Permissions Required for this app to work correctly!",
                    REQUEST_CODE_LOCATION_PERMISSIONS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } else {
                EasyPermissions.requestPermissions(
                    this,
                    "Location Permissions Required for this app to work correctly!",
                    REQUEST_CODE_LOCATION_PERMISSIONS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) { }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this)
                .build()
                .show()
        } else {
            requestLocationPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}