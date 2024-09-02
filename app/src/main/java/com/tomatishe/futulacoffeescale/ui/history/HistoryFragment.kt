package com.tomatishe.futulacoffeescale.ui.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.EmptyResultSetException
import com.google.android.material.snackbar.Snackbar
import com.tomatishe.futulacoffeescale.Dependencies
import com.tomatishe.futulacoffeescale.R
import com.tomatishe.futulacoffeescale.databinding.FragmentHistoryBinding
import com.tomatishe.futulacoffeescale.databinding.ItemWeightDataBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

data class wRecord(
    val id: Long,
    val dateString: String,
    val summaryString: String,
)

class WeightAdapter : RecyclerView.Adapter<WeightAdapter.WeightViewHolder>() {
    lateinit var navController: NavController
    lateinit var scope: CoroutineScope

    var data: MutableList<wRecord> = mutableListOf()
        set(newValue) {
            field = newValue
            // notifyDataSetChanged()
        }

    class WeightViewHolder(val binding: ItemWeightDataBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemWeightDataBinding.inflate(inflater, parent, false)
        return WeightViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: WeightViewHolder, position: Int) {
        val wData = data[position]
        val context = holder.itemView.context
        with(holder.binding) {
            dateTextBlock.text = wData.dateString
            dateTextView.text = wData.summaryString
            deleteImageView.setOnClickListener {
                scope.launch {
                    try {
                        Dependencies.weightRecordRepositoryExtra.deleteWeightRecordExtraDataByWeightId(
                            wData.id
                        )
                    } catch (_: Exception) {

                    } finally {
                        Dependencies.weightRecordRepository.deleteWeightRecordDataById(wData.id)
                    }
                }
                data.removeAt(position)
                notifyItemRemoved(position)
            }
        }
        holder.itemView.setOnClickListener {
            val action =
                HistoryFragmentDirections.actionNavHistoryToNavHistoryInfo(historyRecordId = wData.id)
            navController.navigate(action)
        }
    }

}

class HistoryFragment : Fragment() {
    private lateinit var weightRecordsList: RecyclerView
    private lateinit var weightRecordsListData: ArrayList<wRecord>
    private lateinit var adapter: WeightAdapter

    private var _binding: FragmentHistoryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var gotHistory: Boolean = false
        set(value) {
            field = value
            activity?.runOnUiThread {
                if (gotHistory) {
                    adapter.data = weightRecordsListData
                    adapter.notifyDataSetChanged()
                }
            }
        }

    private fun getWeightData() {
        scope.launch {
            val weightRecords = Dependencies.weightRecordRepository.getAllWeightRecordData()
            for (weightRecord in weightRecords) {
                var sumString = ""
                val currId = weightRecord.id
                val backToDate: Date = Date(weightRecord.brewDate)
                val dateBlockFormat = SimpleDateFormat("dd.MM")
                val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
                val flowRateAvgString = "%.1f".format(weightRecord.flowRateAvg)
                val weightRecordString = "%.1f".format(weightRecord.weightRecord)
                val currInfoRecord =
                    Dependencies.weightRecordRepositoryExtra.getOneWeightRecordExtraDataByWeightId(
                        weightRecord.id
                    )
                if (currInfoRecord != null) {
                    if (currInfoRecord.coffeeBean != null && currInfoRecord.coffeeBean.length > 0) {
                        sumString = currInfoRecord.coffeeBean
                    }
                    if (currInfoRecord.coffeeRoaster != null && currInfoRecord.coffeeRoaster.length > 0) {
                        if (sumString.isNotEmpty()) {
                            sumString += " | " + currInfoRecord.coffeeRoaster
                        } else {
                            sumString = currInfoRecord.coffeeRoaster
                        }
                    }
                    if (currInfoRecord.gadgetName != null && currInfoRecord.gadgetName.length > 0) {
                        if (sumString.isNotEmpty()) {
                            sumString += " | " + currInfoRecord.gadgetName
                        } else {
                            sumString = currInfoRecord.gadgetName
                        }
                    }
                }
                if (sumString.length == 0) {
                    sumString = dateFormat.format(backToDate)
                }
                val currRecord: wRecord = wRecord(
                    currId, dateBlockFormat.format(backToDate), sumString
                )
                weightRecordsListData.add(currRecord)
            }

            gotHistory = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.history, menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        gotHistory = false

        weightRecordsListData = ArrayList()

        val manager = LinearLayoutManager(activity?.applicationContext)
        val navController = findNavController(this)

        adapter = WeightAdapter()
        adapter.navController = navController
        adapter.scope = scope

        weightRecordsList = binding.weightRecordsList
        weightRecordsList.layoutManager = manager
        weightRecordsList.adapter = adapter

        getWeightData()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}