package work.racka.runningtracker.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import work.racka.runningtracker.R
import work.racka.runningtracker.databinding.ActivityMainBinding
import work.racka.runningtracker.util.Constants.ACTION_SHOW_TRACKING_FRAGMENT

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        navigateToTrackingFragment(intent)

        setSupportActionBar(viewBinding.toolbar)

        val bottomNavigationView = viewBinding.bottomNavigationView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment)
        val controller = navHostFragment?.findNavController()
        bottomNavigationView.setupWithNavController(controller!!)

        controller
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.settingsFragment, R.id.statisticsFragment, R.id.runFragment ->
                        bottomNavigationView.visibility = View.VISIBLE
                    else -> bottomNavigationView.visibility = View.GONE
                }
            }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragment(intent)
    }

    private fun navigateToTrackingFragment(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            viewBinding.navHostFragment.findNavController()
                .navigate(R.id.action_global_TrackingFragment)
        }
    }
}