package work.racka.runningtracker.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import work.racka.runningtracker.R
import work.racka.runningtracker.databinding.FragmentSetupBinding
import work.racka.runningtracker.util.Constants.KEY_FIRST_TIME_TOGGLE
import work.racka.runningtracker.util.Constants.KEY_NAME
import work.racka.runningtracker.util.Constants.KEY_WEIGHT
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {

    private lateinit var viewBinding: FragmentSetupBinding

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @set:Inject
    var isFirstAppOpen: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isFirstAppOpen) {
            val navOptions: NavOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()

            findNavController()
                .navigate(
                    R.id.action_setupFragment_to_runFragment,
                    savedInstanceState,
                    navOptions
                )

        }

        viewBinding = FragmentSetupBinding.bind(view)

        viewBinding.tvContinue.setOnClickListener {
            val success = writePersonalDataToSharedPref()
            if (success) {
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            } else {
                Snackbar.make(
                    requireView(),
                    "Please Enter All Fields!",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun writePersonalDataToSharedPref(): Boolean {
        val name = viewBinding.etName.text.toString()
        val weight = viewBinding.etWeight.text.toString()
        if (name.isEmpty() || weight.isEmpty()) {
            return false
        }
        sharedPreferences.edit {
            putString(KEY_NAME, name)
            putFloat(KEY_WEIGHT, weight.toFloat())
            putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            apply()
        }
        return true
    }
}