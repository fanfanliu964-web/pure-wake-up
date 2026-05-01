package com.csu.schedule.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses WHERE semester_id = :semesterId")
    fun getCoursesBySemester(semesterId: Long): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE semester_id = :semesterId AND day_of_week = :day")
    fun getCoursesByDay(semesterId: Long, day: Int): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE semester_id = :semesterId AND day_of_week = :day")
    suspend fun getCoursesByDaySync(semesterId: Long, day: Int): List<CourseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(courses: List<CourseEntity>)

    @Query("DELETE FROM courses WHERE semester_id = :semesterId")
    suspend fun deleteBySemester(semesterId: Long)
}
