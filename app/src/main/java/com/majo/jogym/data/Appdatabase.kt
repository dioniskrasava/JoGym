package com.majo.jogym.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.majo.jogym.data.dao.DayDao
import com.majo.jogym.data.dao.ExerciseDao
import com.majo.jogym.data.dao.SetDao
import com.majo.jogym.data.dao.WorkoutDao
import com.majo.jogym.data.model.DayEntity
import com.majo.jogym.data.model.ExerciseEntity
import com.majo.jogym.data.model.SetEntity
import com.majo.jogym.data.model.WorkoutEntity

@Database(
    entities = [
        DayEntity::class,
        WorkoutEntity::class,
        SetEntity::class,
        ExerciseEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun dayDao(): DayDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun setDao(): SetDao
}