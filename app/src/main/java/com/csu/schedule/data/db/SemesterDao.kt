package com.csu.schedule.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SemesterDao {
    @Query("SELECT * FROM semesters WHERE is_active = 1 LIMIT 1")
    fun getActiveSemester(): Flow<SemesterEntity?>

    @Query("SELECT * FROM semesters WHERE is_active = 1 LIMIT 1")
    suspend fun getActiveSemesterSync(): SemesterEntity?

    @Insert
    suspend fun insert(semester: SemesterEntity): Long

    @Query("UPDATE semesters SET is_active = 0")
    suspend fun deactivateAll()
}
