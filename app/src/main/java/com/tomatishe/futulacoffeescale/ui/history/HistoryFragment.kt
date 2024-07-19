package com.tomatishe.futulacoffeescale.ui.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tomatishe.futulacoffeescale.Dependencies
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
            dateTextView.text = wData.dateString
            deleteImageView.setOnClickListener {
                scope.launch {
                    Dependencies.weightRecordRepository.deleteWeightRecordDataById(wData.id)
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
                val currId = weightRecord.id
                val backToDate: Date = Date(weightRecord.brewDate)
                val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
                val flowRateAvgString = "%.1f".format(weightRecord.flowRateAvg)
                val weightRecordString = "%.1f".format(weightRecord.weightRecord)
                val sumString =
                    "T: ${weightRecord.timeString} FR: ${flowRateAvgString} W: ${weightRecordString} ${weightRecord.weightUnit}"
                val currRecord: wRecord = wRecord(
                    currId, dateFormat.format(backToDate), sumString
                )
                weightRecordsListData.add(currRecord)
            }

            gotHistory = true
        }
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