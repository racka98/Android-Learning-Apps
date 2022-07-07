/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.databinding.FragmentSleepTrackerBinding
import com.google.android.material.snackbar.Snackbar

/**
 * A fragment with buttons to record start and end times for sleep, which are saved in
 * a database. Cumulative data is displayed in a simple scrollable TextView.
 * (Because we have not learned about RecyclerView yet.)
 */
class SleepTrackerFragment : Fragment() {

    /**
     * Called when the Fragment is ready to display content to the screen.
     *
     * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentSleepTrackerBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_sleep_tracker, container, false)

        //Getting a reference to the application context
        val application = requireNotNull(this.activity).application
        //Defining a dataSource
        val dataSource = SleepDatabase.getInstance(application).sleepDatabaseDao
        //Instance of ViewModelFactory
        val viewModelFactory = SleepTrackerViewModelFactory(dataSource, application)
        //Setting viewProviders for SleepTrackerViewModel
        val viewModel = ViewModelProvider(this, viewModelFactory)
                .get(SleepTrackerViewModel::class.java)

        //Setting the lifecycle owner
        binding.lifecycleOwner = this
        binding.sleepTrackerViewModel = viewModel

        //Observer for navigateToSleepQuality
        viewModel.navigateToSleepQuality.observe(viewLifecycleOwner, Observer { night ->
            night?.let {
                this.findNavController().navigate(
                        SleepTrackerFragmentDirections.actionSleepTrackerFragmentToSleepQualityFragment(night.nightId)
                )
                viewModel.doneNavigating()
            }
        })

        //Observer for showSnackbarEvent to show a Snackbar
        viewModel.showSnackbarEvent.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                Snackbar.make(
                        requireActivity().findViewById(R.id.constraintLayout),
                        getString(R.string.cleared_message),
                        Snackbar.LENGTH_SHORT
                ).show()
                viewModel.doneShowingSnackbar()
            }
        })

        //Observer for navigating to sleepDetail fragment
        viewModel.navigateToSleepDataQuality.observe(viewLifecycleOwner, Observer { night ->
            night?.let {
//                if (findNavController().currentDestination?.id == R.id.sleep_tracker_fragment) {
//                    findNavController().navigate(
//                        SleepTrackerFragmentDirections.actionSleepTrackerFragmentToSleepDetailFragment(night)
//                    )
//                }

                this.findNavController().navigate(
                    SleepTrackerFragmentDirections.actionSleepTrackerFragmentToSleepDetailFragment(night)
                )
                viewModel.onSleepDataQualityNavigated()
            }
        })

        //Creating Grid layout
        val manager = GridLayoutManager(activity, 3)
        binding.sleepList.layoutManager = manager

        //set the adapter from SleepNightAdapter
        val adapter = SleepNightAdapter(SleepNightListener { nightId ->
            viewModel.onSleepNightClicked(nightId)
        })
        //Set the adapter to the RecyclerView sleepList
        binding.sleepList.adapter = adapter

        //Observer for setting the adapter
        viewModel.nights.observe(viewLifecycleOwner, Observer {
            it?.let {
                //Replace this with adapter.submitList()
                //adapter.data = it
                adapter.submitList(it)
            }
        })


        return binding.root
    }
}
