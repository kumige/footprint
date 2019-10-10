package com.example.sensorapp

import android.content.Context
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.util.GeoPoint
import java.util.Collections.emptyList



@Database(
    entities = [User::class, History::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase(){
    abstract fun dao(): dao

    companion object {
        @Volatile private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context)= instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also { instance = it}
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
            AppDatabase::class.java, "user.db")
            .build()
    }
}


@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    @ColumnInfo(name = "user") var user: String
)

@Entity(tableName = "history")
data class History(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    @ColumnInfo(name = "start_time") var startTime: String,
    @ColumnInfo(name = "end_time") var endTime: String,
    @ColumnInfo(name = "duration") var duration: Int,
    @ColumnInfo(name = "distance") var distance: Int,
    @TypeConverters(DbTypeConverters::class)
    @ColumnInfo(name = "route") var route: String

)

@Dao
interface dao {
    @Query("SELECT user FROM user")
    fun getUsername(): String

    @Query("SELECT * FROM history")
    fun getAllHistory(): MutableList<History>

    @Query("SELECT * FROM history WHERE id = :historyId")
    fun getHistory(historyId: Int): MutableList<History>

    @Insert
    fun insertName(user: User)

    @Insert
    fun insertRun(data: History)

    @Query("DELETE FROM history WHERE id = :historyId")
    fun deleteFromHistory(historyId: Int)

    @Query("UPDATE user SET user=:name WHERE id=1")
    fun updateUsername(name: String)

    @Query("SELECT * FROM user")
    fun getUser(): List<User>

    @Delete
    fun deleteUserName(vararg user: User)

}

class DbTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun stringToGeoPointList(data: String?): List<GeoPoint> {
        if (data == null) {
            return emptyList()
        }

        val listType = object : TypeToken<List<GeoPoint>>() {

        }.type

        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun geoPointListToString(list: List<GeoPoint>): String {
        return gson.toJson(list)
    }

}