package com.tomatishe.futulacoffeescale.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.tomatishe.futulacoffeescale.R
import com.tomatishe.futulacoffeescale.databinding.FragmentSettingsBinding
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.google.android.material.materialswitch.MaterialSwitch

class SettingsFragment : Fragment() {

    private lateinit var autoAllLayout: ConstraintLayout
    private lateinit var autoStartLayout: ConstraintLayout
    private lateinit var ignoreDoseLayout: ConstraintLayout
    private lateinit var autoDoseLayout: ConstraintLayout
    private lateinit var autoTareLayout: ConstraintLayout
    private lateinit var autoSwitchesLayout: ConstraintLayout
    private lateinit var autoButtonsLayout: ConstraintLayout

    private lateinit var autoFuncSettingsSwitch: MaterialSwitch
    private lateinit var autoStartSettingsSwitch: MaterialSwitch
    private lateinit var ignoreDoseSettingsSwitch: MaterialSwitch
    private lateinit var autoDoseSettingsSwitch: MaterialSwitch
    private lateinit var autoTareSettingsSwitch: MaterialSwitch
    private lateinit var autoHideSwitchesSettingsSwitch: MaterialSwitch
    private lateinit var autoHideButtonsSettingsSwitch: MaterialSwitch

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val settingsViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        autoAllLayout = binding.autoAllLayout
        autoStartLayout = binding.autoStartLayout
        ignoreDoseLayout = binding.ignoreDoseLayout
        autoDoseLayout = binding.autoDoseLayout
        autoTareLayout = binding.autoTareLayout
        autoSwitchesLayout = binding.autoSwitchesLayout
        autoButtonsLayout = binding.autoButtonsLayout

        autoFuncSettingsSwitch = binding.autoFuncSettingsSwitch
        autoStartSettingsSwitch = binding.autoStartSettingsSwitch
        ignoreDoseSettingsSwitch = binding.ignoreDoseSettingsSwitch
        autoDoseSettingsSwitch = binding.autoDoseSettingsSwitch
        autoTareSettingsSwitch = binding.autoTareSettingsSwitch
        autoHideSwitchesSettingsSwitch = binding.autoHideSwitchesSettingsSwitch
        autoHideButtonsSettingsSwitch = binding.autoHideButtonsSettingsSwitch

        autoFuncSettingsSwitch.setOnClickListener {
            if (autoFuncSettingsSwitch.isChecked) {
                autoStartLayout.visibility = View.VISIBLE
                if (autoStartSettingsSwitch.isChecked) {
                    ignoreDoseLayout.visibility = View.VISIBLE
                }
                autoDoseLayout.visibility = View.VISIBLE
                autoTareLayout.visibility = View.VISIBLE
            } else {
                autoStartLayout.visibility = View.GONE
                ignoreDoseLayout.visibility = View.GONE
                autoDoseLayout.visibility = View.GONE
                autoTareLayout.visibility = View.GONE
            }
        }

        autoStartSettingsSwitch.setOnClickListener {
            if (autoStartSettingsSwitch.isChecked) {
                ignoreDoseLayout.visibility = View.VISIBLE
            } else {
                ignoreDoseLayout.visibility = View.GONE
                if (!autoDoseSettingsSwitch.isChecked && !autoTareSettingsSwitch.isChecked) {
                    autoFuncSettingsSwitch.performClick()
                }
            }
        }

        autoDoseSettingsSwitch.setOnClickListener {
            if (!autoStartSettingsSwitch.isChecked && !autoDoseSettingsSwitch.isChecked && !autoTareSettingsSwitch.isChecked) {
                autoFuncSettingsSwitch.performClick()
            }
        }

        autoTareSettingsSwitch.setOnClickListener {
            if (!autoStartSettingsSwitch.isChecked && !autoDoseSettingsSwitch.isChecked && !autoTareSettingsSwitch.isChecked) {
                autoFuncSettingsSwitch.performClick()
            }
        }

        //val textView: TextView = binding.textSlideshow
        //settingsViewModel.text.observe(viewLifecycleOwner) {
        //    textView.text = it
        //}
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}