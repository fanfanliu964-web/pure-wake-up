package com.csu.schedule.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SemesterDao {
    @Query("SELECT * FROM semesters ORDER BY id DESC")
    fun getAllSemesters(): Flow<List<SemesterEntity>>

    @Query("SELECT * FROM semesters WHERE is_active = 1 LIMIT 1")
    fun getActiveSemester(): Flow<SemesterEntity?>

    @Query("SELECT * FROM semesters WHERE is_active = 1 LIMIT 1")
    suspend fun getActiveSemesterSync(): SemesterEntity?

    @Query("SELECT * FROM semesters WHERE id = :id LIMIT 1")
    suspend fun getSemesterByIdSync(id: Long): SemesterEntity?

    @Query("SELECT * FROM semesters WHERE name = :name ORDER BY id DESC LIMIT 1")
    suspend fun getSemesterByNameSync(name: String): SemesterEntity?

    @Query("SELECT * FROM semesters WHERE id != :excludeId ORDER BY id DESC LIMIT 1")
    suspend fun getLatestExceptSync(excludeId: Long): SemesterEntity?

    @Insert
    suspend fun insert(semester: SemesterEntity): Long

    @Update
    suspend fun update(semester: SemesterEntity)

    @Query("UPDATE semesters SET is_active = 0")
    suspend fun deactivateAll()

    @Query("UPDATE semesters SET is_active = CASE WHEN id = :semesterId THEN 1 ELSE 0 END")
    suspend fun activateOnly(semesterId: Long)

    @Query("DELETE FROM semesters WHERE id = :semesterId")
    suspend fun deleteById(semesterId: Long)
}
