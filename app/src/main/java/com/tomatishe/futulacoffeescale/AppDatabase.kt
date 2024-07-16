package com.tomatishe.futulacoffeescale

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Entity(tableName = "weight_history")
data class WeightRecordDbEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "brew_date") val brewDate: Long,
    @ColumnInfo(name = "weight_unit") val weightUnit: String,
    @ColumnInfo(name = "dose_record") val doseRecord: Float,
    @ColumnInfo(name = "weight_record") val weightRecord: Float,
    @ColumnInfo(name = "weight_log") val weightLog: String,
    @ColumnInfo(name = "flow_rate") val flowRate: Float,
    @ColumnInfo(name = "flow_rate_avg") val flowRateAvg: Float,
    @ColumnInfo(name = "flow_rate_log") val flowRateLog: String,
    @ColumnInfo(name = "time_string") val timeString: String,
    @ColumnInfo(name = "brew_ratio_string") val brewRatioString: String,
)

data class WeightRecordInfoTuple(
    val id: Long,
    @ColumnInfo(name = "brew_date") val brewDate: Long,
    @ColumnInfo(name = "weight_unit") val weightUnit: String,
    @ColumnInfo(name = "dose_record") val doseRecord: Float,
    @ColumnInfo(name = "weight_record") val weightRecord: Float,
    @ColumnInfo(name = "weight_log") val weightLog: String,
    @ColumnInfo(name = "flow_rate") val flowRate: Float,
    @ColumnInfo(name = "flow_rate_avg") val flowRateAvg: Float,
    @ColumnInfo(name = "flow_rate_log") val flowRateLog: String,
    @ColumnInfo(name = "time_string") val timeString: String,
    @ColumnInfo(name = "brew_ratio_string") val brewRatioString: String,
)

data class WeightRecord(
    val brewDate: Long,
    val weightUnit: String,
    val doseRecord: Float,
    val weightRecord: Float,
    val weightLog: String,
    val flowRate: Float,
    val flowRateAvg: Float,
    val flowRateLog: String,
    val timeString: String,
    val brewRatioString: String,
) {

    fun toWeightRecordDbEntity(): WeightRecordDbEntity = WeightRecordDbEntity(
        id = 0,
        brewDate = brewDate,
        weightUnit = weightUnit,
        doseRecord = doseRecord,
        weightRecord = weightRecord,
        weightLog = weightLog,
        flowRate = flowRate,
        flowRateAvg = flowRateAvg,
        flowRateLog = flowRateLog,
        timeString = timeString,
        brewRatioString = brewRatioString,
    )
}


@Dao
interface WeightRecordDao {
    @Insert(entity = WeightRecordDbEntity::class)
    fun insertWeightRecordData(weightRecord: WeightRecordDbEntity)

    @Query(
        "SELECT id, brew_date, weight_unit, dose_record, weight_record,\n " +
                "weight_log, flow_rate, flow_rate_avg, flow_rate_log,\n" +
                "time_string, brew_ratio_string FROM weight_history \n" +
                "ORDER BY id DESC"
    )
    fun getAllWeightRecordData(): List<WeightRecordInfoTuple>

    @Query(
        "SELECT id, brew_date, weight_unit, dose_record, weight_record,\n " +
                "weight_log, flow_rate, flow_rate_avg, flow_rate_log,\n" +
                "time_string, brew_ratio_string FROM weight_history \n" +
                "WHERE id = :wightRecordId"
    )
    fun getOneWeightRecordDataById(wightRecordId: Long): WeightRecordInfoTuple

    @Query("DELETE FROM weight_history WHERE id = :wightRecordId")
    fun deleteWeightRecordDataById(wightRecordId: Long)
}

@Database(
    version = 1,
    entities = [
        WeightRecordDbEntity::class,
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getWeightRecordDao(): WeightRecordDao
}

object Dependencies {

    private lateinit var applicationContext: Context

    fun init(context: Context) {
        applicationContext = context
    }

    private val appDatabase: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext, AppDatabase::class.java, "futula_coffee_scale_database.db"
        ).build()
    }

    val weightRecordRepository: WeightRecordRepository by lazy {
        WeightRecordRepository(appDatabase.getWeightRecordDao())
    }
}

class WeightRecordRepository(private val weightRecordDao: WeightRecordDao) {

    suspend fun insertWeightRecordData(weightRecordDbEntity: WeightRecordDbEntity) {
        withContext(Dispatchers.IO) {
            weightRecordDao.insertWeightRecordData(weightRecordDbEntity)
        }
    }

    suspend fun getAllWeightRecordData(): List<WeightRecordInfoTuple> {
        return withContext(Dispatchers.IO) {
            return@withContext weightRecordDao.getAllWeightRecordData()
        }
    }

    suspend fun getOneWeightRecordDataById(id: Long): WeightRecordInfoTuple {
        return withContext(Dispatchers.IO) {
            return@withContext weightRecordDao.getOneWeightRecordDataById(id)
        }
    }

    suspend fun deleteWeightRecordDataById(id: Long) {
        withContext(Dispatchers.IO) {
            weightRecordDao.deleteWeightRecordDataById(id)
        }
    }
}