package com.pomodoroalert.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {

    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: AlarmEntity)

    @Update
    suspend fun update(alarm: AlarmEntity)

    @Delete
    suspend fun delete(alarm: AlarmEntity)

    @Query("SELECT * FROM alarms WHERE alarmId = :id")
    suspend fun getById(id: String): AlarmEntity?

    @Query("UPDATE alarms SET isEnabled = :enabled WHERE alarmId = :id")
    suspend fun setEnabled(id: String, enabled: Boolean)

    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    suspend fun getAllAlarmsOnce(): List<AlarmEntity>
}
