package com.csu.schedule.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses WHERE semester_id = :semesterId")
    fun getCoursesBySemester(semesterId: Long): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE semester_id = :semesterId AND day_of_week = :day")
    fun getCoursesByDay(semesterId: Long, day: Int): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE semester_id = :semesterId AND day_of_week = :day")
    suspend fun getCoursesByDaySync(semesterId: Long, day: Int): List<CourseEntity>

    @Query("SELECT * FROM courses WHERE semester_id = :semesterId")
    suspend fun getCoursesBySemesterSync(semesterId: Long): List<CourseEntity>

    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    suspend fun getCourseByIdSync(id: Long): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(courses: List<CourseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: CourseEntity): Long

    @Update
    suspend fun update(course: CourseEntity)

    @Delete
    suspend fun delete(course: CourseEntity)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM courses WHERE semester_id = :semesterId")
    suspend fun deleteBySemester(semesterId: Long)
}
