package work.racka.runningtracker.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import work.racka.runningtracker.R
import work.racka.runningtracker.databinding.FragmentSettingsBinding
import work.racka.runningtracker.util.Constants.KEY_NAME
import work.racka.runningtracker.util.Constants.KEY_WEIGHT
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var binding: FragmentSettingsBinding

    @Inject
    lateinit var sharedPref: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)

        loadFieldsFromSharedPref()

        binding.btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharedPref()
            if (success) {
                Snackbar.make(
                    requireView(),
                    "Changes have been saved!",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                Snackbar.make(
                    requireView(),
                    "Please fill out all fields!!",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadFieldsFromSharedPref() {
        val name = sharedPref.getString(KEY_NAME, "")
        val weight = sharedPref.getFloat(KEY_WEIGHT, 0f)
        binding.etName.setText(name)
        binding.etWeight.setText(weight.toString())
    }

    private fun applyChangesToSharedPref(): Boolean {
        val nameText = binding.etName.text.toString()
        val weightText = binding.etWeight.text.toString()
        if (nameText.isEmpty() || weightText.isEmpty()) {
            return false
        }
        sharedPref.edit {
            putString(KEY_NAME, nameText)
            putFloat(KEY_WEIGHT, weightText.toFloat())
            apply()
        }
        setToolbarTitleText()
        return true
    }

    private fun setToolbarTitleText() {
        val name = sharedPref.getString(KEY_NAME, "")
        val toolbarText = "Let's Go, $name!"
        val toolbarTitle = requireActivity()
            .findViewById<MaterialTextView>(R.id.tvToolbarTitle)
        toolbarTitle.text = toolbarText
    }
}