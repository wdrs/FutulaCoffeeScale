package com.tomatishe.futulacoffeescale.ui.historyExtra

import android.R
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.tomatishe.futulacoffeescale.Dependencies
import com.tomatishe.futulacoffeescale.WeightRecordExtra
import com.tomatishe.futulacoffeescale.WeightRecordInfoTupleBeans
import com.tomatishe.futulacoffeescale.WeightRecordInfoTupleExtra
import com.tomatishe.futulacoffeescale.WeightRecordInfoTupleGadgets
import com.tomatishe.futulacoffeescale.WeightRecordInfoTupleGrindLevels
import com.tomatishe.futulacoffeescale.WeightRecordInfoTupleGrinders
import com.tomatishe.futulacoffeescale.WeightRecordInfoTupleRoasters
import com.tomatishe.futulacoffeescale.WeightRecordInfoTupleWaterTemps
import com.tomatishe.futulacoffeescale.databinding.FragmentHistoryExtraBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class HistoryExtraFragment : Fragment() {

    private var _binding: FragmentHistoryExtraBinding? = null
    private val binding get() = _binding!!

    private var currentExtraId: Long = -1
    private var historyRecordId: Long? = -1

    private lateinit var coffeeBeanDataAuto: AppCompatAutoCompleteTextView
    private lateinit var coffeeRoastDataAuto: AppCompatAutoCompleteTextView
    private lateinit var coffeeGrinderDataAuto: AppCompatAutoCompleteTextView
    private lateinit var coffeeGrinderLevelAuto: AppCompatAutoCompleteTextView
    private lateinit var gadgetNameAuto: AppCompatAutoCompleteTextView
    private lateinit var waterTempAuto: AppCompatAutoCompleteTextView
    private lateinit var extraInfoAuto: AppCompatEditText

    private lateinit var saveButton: Button
    private lateinit var clearButton: Button

    companion object {
        fun newInstance(historyRecordId: Long): Fragment {
            val fragment = HistoryExtraFragment()
            fragment.arguments = Bundle().apply {
                putLong("historyRecordId", historyRecordId)
            }
            return fragment
        }
    }

    private val viewModel: HistoryExtraViewModel by viewModels()

    private suspend fun deleteCurrentExtraData() {
        Handler(Looper.getMainLooper()).post {
            coffeeBeanDataAuto.text = null
            coffeeRoastDataAuto.text = null
            coffeeGrinderDataAuto.text = null
            coffeeGrinderLevelAuto.text = null
            gadgetNameAuto.text = null
            waterTempAuto.text = null
            extraInfoAuto.text = null
        }
        if (currentExtraId >= 0) {
            Dependencies.weightRecordRepositoryExtra.deleteWeightRecordExtraDataById(currentExtraId)
            currentExtraId = -1
            Snackbar.make(
                binding.root,
                com.tomatishe.futulacoffeescale.R.string.action_deleted_extra,
                Snackbar.LENGTH_SHORT
            ).setAnchorView(binding.divider4).show()
            saveButton.text = getString(com.tomatishe.futulacoffeescale.R.string.extra_save_button)
        }
    }

    private suspend fun saveCurrentExtraData() {
        val currentExtraData = WeightRecordExtra(
            historyRecordId!!,
            coffeeBeanDataAuto.text.toString(),
            coffeeGrinderDataAuto.text.toString(),
            coffeeGrinderLevelAuto.text.toString(),
            gadgetNameAuto.text.toString(),
            waterTempAuto.text.toString(),
            extraInfoAuto.text.toString(),
            coffeeRoastDataAuto.text.toString()
        )
        if (currentExtraId >= 0) {
            Dependencies.weightRecordRepositoryExtra.updateWeightRecordExtraDataById(
                currentExtraId,
                currentExtraData.toWeightRecordDbEntityExtra()
            )
            Snackbar.make(
                binding.root,
                com.tomatishe.futulacoffeescale.R.string.action_updated_extra,
                Snackbar.LENGTH_SHORT
            ).setAnchorView(binding.divider4).show()
        } else {
            Dependencies.weightRecordRepositoryExtra.insertWeightRecordExtraData(currentExtraData.toWeightRecordDbEntityExtra())
            val brewDataExtra: WeightRecordInfoTupleExtra =
                Dependencies.weightRecordRepositoryExtra.getOneWeightRecordExtraDataByWeightId(
                    historyRecordId!!
                )
            currentExtraId = brewDataExtra.id
            Snackbar.make(
                binding.root,
                com.tomatishe.futulacoffeescale.R.string.action_saved_extra,
                Snackbar.LENGTH_SHORT
            ).setAnchorView(binding.divider4).show()
            saveButton.text =
                getString(com.tomatishe.futulacoffeescale.R.string.extra_save_button_alt)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryExtraBinding.inflate(inflater, container, false)
        val root: View = binding.root

        historyRecordId = this.arguments?.getLong("historyRecordId")

        GlobalScope.launch {
            if (historyRecordId != null) {
                val brewDataExtra: WeightRecordInfoTupleExtra =
                    Dependencies.weightRecordRepositoryExtra.getOneWeightRecordExtraDataByWeightId(
                        historyRecordId!!
                    )
                val beanListData: List<WeightRecordInfoTupleBeans> =
                    Dependencies.weightRecordRepositoryExtra.getDistinctCoffeeBeans()
                val roasterListData: List<WeightRecordInfoTupleRoasters> =
                    Dependencies.weightRecordRepositoryExtra.getDistinctCoffeeRoasters()
                val grinderListData: List<WeightRecordInfoTupleGrinders> =
                    Dependencies.weightRecordRepositoryExtra.getDistinctGrinders()
                val grinderLevelListData: List<WeightRecordInfoTupleGrindLevels> =
                    Dependencies.weightRecordRepositoryExtra.getDistinctGrinderLevels()
                val gadgetListData: List<WeightRecordInfoTupleGadgets> =
                    Dependencies.weightRecordRepositoryExtra.getDistinctGadgets()
                val waterTempListData: List<WeightRecordInfoTupleWaterTemps> =
                    Dependencies.weightRecordRepositoryExtra.getDistinctWaterTemps()

                val beanDataFinal = beanListData?.map { it.coffeeBean }
                val roasterDataFinal = roasterListData?.map { it.coffeeRoaster }
                val grinderDataFinal = grinderListData?.map { it.coffeeGrinder }
                val grinderLevelDataFinal = grinderLevelListData?.map { it.coffeeGrinderLevel }
                val gadgetDataFinal = gadgetListData?.map { it.gadgetName }
                val waterTempDataFinal = waterTempListData?.map { it.waterTemp }

                coffeeBeanDataAuto = binding.coffeeBeanDataAuto
                coffeeRoastDataAuto = binding.coffeeRoastDataAuto
                coffeeGrinderDataAuto = binding.coffeeGrinderDataAuto
                coffeeGrinderLevelAuto = binding.coffeeGrinderLevelAuto
                gadgetNameAuto = binding.gadgetNameAuto
                waterTempAuto = binding.waterTempAuto
                extraInfoAuto = binding.extraInfoAuto

                saveButton = binding.saveButton
                clearButton = binding.clearButton

                Handler(Looper.getMainLooper()).post {
                    if (beanDataFinal != null) {
                        coffeeBeanDataAuto.setAdapter(
                            ArrayAdapter(
                                binding.root.context,
                                R.layout.simple_list_item_1, beanDataFinal.toMutableList()
                            )
                        )
                    }

                    if (roasterDataFinal != null) {
                        coffeeRoastDataAuto.setAdapter(
                            ArrayAdapter(
                                binding.root.context,
                                R.layout.simple_list_item_1, roasterDataFinal.toMutableList()
                            )
                        )
                    }

                    if (grinderDataFinal != null) {
                        coffeeGrinderDataAuto.setAdapter(
                            ArrayAdapter(
                                binding.root.context,
                                R.layout.simple_list_item_1, grinderDataFinal.toMutableList()
                            )
                        )
                    }

                    if (grinderLevelDataFinal != null) {
                        coffeeGrinderLevelAuto.setAdapter(
                            ArrayAdapter(
                                binding.root.context,
                                R.layout.simple_list_item_1, grinderLevelDataFinal.toMutableList()
                            )
                        )
                    }

                    if (gadgetDataFinal != null) {
                        gadgetNameAuto.setAdapter(
                            ArrayAdapter(
                                binding.root.context,
                                R.layout.simple_list_item_1, gadgetDataFinal.toMutableList()
                            )
                        )
                    }

                    if (waterTempDataFinal != null) {
                        waterTempAuto.setAdapter(
                            ArrayAdapter(
                                binding.root.context,
                                R.layout.simple_list_item_1, waterTempDataFinal.toMutableList()
                            )
                        )
                    }
                }

                if (brewDataExtra != null) {
                    currentExtraId = brewDataExtra.id
                    Handler(Looper.getMainLooper()).post {
                        coffeeBeanDataAuto.setText(brewDataExtra.coffeeBean)
                        coffeeRoastDataAuto.setText(brewDataExtra.coffeeRoaster)
                        coffeeGrinderDataAuto.setText(brewDataExtra.coffeeGrinder)
                        coffeeGrinderLevelAuto.setText(brewDataExtra.coffeeGrinderLevel)
                        gadgetNameAuto.setText(brewDataExtra.gadgetName)
                        waterTempAuto.setText(brewDataExtra.waterTemp)
                        extraInfoAuto.setText(brewDataExtra.extraInfo)
                        saveButton.text =
                            getString(com.tomatishe.futulacoffeescale.R.string.extra_save_button_alt)
                    }
                }

                saveButton.setOnClickListener {
                    GlobalScope.launch {
                        saveCurrentExtraData()
                    }
                }

                clearButton.setOnClickListener {
                    GlobalScope.launch {
                        deleteCurrentExtraData()
                    }
                }
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}