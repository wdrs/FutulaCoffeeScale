package com.tomatishe.futulacoffeescale

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

@Entity(
    tableName = "weight_history_extra",
    foreignKeys = [
        ForeignKey(
            entity = WeightRecordDbEntity::class,
            parentColumns = ["id"],
            childColumns = ["weight_id"]
        )
    ]
)
data class WeightRecordDbEntityExtra(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "weight_id") val weightId: Long,
    @ColumnInfo(name = "coffee_bean") val coffeeBean: String?,
    @ColumnInfo(name = "coffee_grinder") val coffeeGrinder: String?,
    @ColumnInfo(name = "coffee_grinder_level") val coffeeGrinderLevel: String?,
    @ColumnInfo(name = "gadget_name") val gadgetName: String?,
    @ColumnInfo(name = "water_temp") val waterTemp: String?,
    @ColumnInfo(name = "extra_info") val extraInfo: String?,
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

data class WeightRecordInfoTupleExtra(
    val id: Long,
    @ColumnInfo(name = "weight_id") val weightId: Long,
    @ColumnInfo(name = "coffee_bean") val coffeeBean: String?,
    @ColumnInfo(name = "coffee_grinder") val coffeeGrinder: String?,
    @ColumnInfo(name = "coffee_grinder_level") val coffeeGrinderLevel: String?,
    @ColumnInfo(name = "gadget_name") val gadgetName: String?,
    @ColumnInfo(name = "water_temp") val waterTemp: String?,
    @ColumnInfo(name = "extra_info") val extraInfo: String?,
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

data class WeightRecordExtra(
    val weightId: Long,
    val coffeeBean: String?,
    val coffeeGrinder: String?,
    val coffeeGrinderLevel: String?,
    val gadgetName: String?,
    val waterTemp: String?,
    val extraInfo: String?,
) {

    fun toWeightRecordDbEntityExtra(): WeightRecordDbEntityExtra = WeightRecordDbEntityExtra(
        id = 0,
        weightId = weightId,
        coffeeBean = coffeeBean,
        coffeeGrinder = coffeeGrinder,
        coffeeGrinderLevel = coffeeGrinderLevel,
        gadgetName = gadgetName,
        waterTemp = waterTemp,
        extraInfo = extraInfo,
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

@Dao
interface WeightRecordExtraDao {
    @Insert(entity = WeightRecordDbEntityExtra::class)
    fun insertWeightRecordExtraData(weightRecordExtra: WeightRecordDbEntityExtra)

    @Query(
        "SELECT id, weight_id, coffee_bean, coffee_grinder, coffee_grinder_level,\n " +
                "gadget_name, water_temp, extra_info\n" +
                "FROM weight_history_extra \n" +
                "ORDER BY id DESC"
    )
    fun getAllWeightRecordExtraData(): List<WeightRecordInfoTupleExtra>

    @Query(
        "SELECT id, weight_id, coffee_bean, coffee_grinder, coffee_grinder_level,\n " +
                "gadget_name, water_temp, extra_info\n" +
                "FROM weight_history_extra \n" +
                "WHERE id = :weightExtraRecordId"
    )
    fun getOneWeightRecordExtraDataById(weightExtraRecordId: Long): WeightRecordInfoTupleExtra

    @Query(
        "SELECT id, weight_id, coffee_bean, coffee_grinder, coffee_grinder_level,\n " +
                "gadget_name, water_temp, extra_info\n" +
                "FROM weight_history_extra \n" +
                "WHERE weight_id = :weightRecordId"
    )
    fun getOneWeightRecordExtraDataByWeightId(weightRecordId: Long): WeightRecordInfoTupleExtra

    @Query("DELETE FROM weight_history_extra WHERE id = :weightExtraRecordId")
    fun deleteWeightRecordExtraDataById(weightExtraRecordId: Long)

    @Query("DELETE FROM weight_history_extra WHERE weight_id = :weightRecordId")
    fun deleteWeightRecordExtraDataByWeightId(weightRecordId: Long)
}

@Database(
    version = 2,
    entities = [
        WeightRecordDbEntity::class,
        WeightRecordDbEntityExtra::class,
    ],
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getWeightRecordDao(): WeightRecordDao
    abstract fun getWeightRecordExtraDao(): WeightRecordExtraDao
}

object Dependencies {

    private lateinit var applicationContext: Context

    fun init(context: Context) {
        applicationContext = context
    }

    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `weight_history_extra`" +
                        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `weight_id` INTEGER NOT NULL," +
                        " `coffee_bean` TEXT, `coffee_grinder` TEXT, `coffee_grinder_level` TEXT," +
                        " `gadget_name` TEXT, `water_temp` TEXT, `extra_info` TEXT," +
                        " FOREIGN KEY(`weight_id`) REFERENCES `weight_history`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )"
            )
        }
    }

    private val appDatabase: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext, AppDatabase::class.java, "futula_coffee_scale_database.db"
        ).fallbackToDestructiveMigration().allowMainThreadQueries().addMigrations(MIGRATION_1_2)
            .build()
    }

    val weightRecordRepository: WeightRecordRepository by lazy {
        WeightRecordRepository(appDatabase.getWeightRecordDao())
    }

    val weightRecordRepositoryExtra: WeightRecordRepositoryExtra by lazy {
        WeightRecordRepositoryExtra(appDatabase.getWeightRecordExtraDao())
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

class WeightRecordRepositoryExtra(private val weightRecordExtraDao: WeightRecordExtraDao) {

    suspend fun insertWeightRecordExtraData(weightRecordDbEntityExtra: WeightRecordDbEntityExtra) {
        withContext(Dispatchers.IO) {
            weightRecordExtraDao.insertWeightRecordExtraData(weightRecordDbEntityExtra)
        }
    }

    suspend fun getAllWeightRecordExtraData(): List<WeightRecordInfoTupleExtra> {
        return withContext(Dispatchers.IO) {
            return@withContext weightRecordExtraDao.getAllWeightRecordExtraData()
        }
    }

    suspend fun getOneWeightRecordExtraDataById(id: Long): WeightRecordInfoTupleExtra {
        return withContext(Dispatchers.IO) {
            return@withContext weightRecordExtraDao.getOneWeightRecordExtraDataById(id)
        }
    }

    suspend fun getOneWeightRecordExtraDataByWeightId(id: Long): WeightRecordInfoTupleExtra {
        return withContext(Dispatchers.IO) {
            return@withContext weightRecordExtraDao.getOneWeightRecordExtraDataByWeightId(id)
        }
    }

    suspend fun deleteWeightRecordExtraDataById(id: Long) {
        withContext(Dispatchers.IO) {
            weightRecordExtraDao.deleteWeightRecordExtraDataById(id)
        }
    }

    suspend fun deleteWeightRecordExtraDataByWeightId(id: Long) {
        withContext(Dispatchers.IO) {
            weightRecordExtraDao.deleteWeightRecordExtraDataByWeightId(id)
        }
    }
}
