package com.example.sensorapp

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import org.threeten.bp.format.DateTimeFormatter
import java.sql.Date
import java.sql.Time
import java.sql.Timestamp
import java.text.DateFormat
import java.time.LocalDate
import java.time.OffsetDateTime

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
    @ColumnInfo(name = "date") var date: String,
    @ColumnInfo(name = "duration") var duration: Int,
    @ColumnInfo(name = "length") var length: Int
    //Kartta?
)

@Dao
interface dao {
    @Query("SELECT user FROM user")
    fun getUsername(): String

    @Query("SELECT * FROM history")
    fun getAllHistory(): List<History>

    @Query("SELECT * FROM history WHERE id = :historyId")
    fun getHistory(historyId: Int): List<History>

    @Insert
    fun insertName(user: User)

    @Insert
    fun insertRun(data: History)

    /*@Delete
    fun delete(todo: TodoEntity)

    @Update
    fun updateTodo(vararg todos: TodoEntity)*/
}