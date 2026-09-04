package com.majo.jogym.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.majo.jogym.data.model.DayEntity
import com.majo.jogym.data.model.ExerciseEntity
import com.majo.jogym.data.model.SetEntity
import com.majo.jogym.data.model.WorkoutEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(exercise: ExerciseEntity): Long

    @Query("SELECT * FROM exercises ORDER BY muscleGroup, name")
    fun observeAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' ORDER BY name")
    fun search(query: String): Flow<List<ExerciseEntity>>
}

@Dao
interface DayDao {
    @Query("SELECT * FROM days WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: LocalDate): DayEntity?

    @Insert
    suspend fun insert(day: DayEntity): Long

    /** Возвращает id дня, создавая запись, если ее еще нет. */
    suspend fun getOrCreate(date: LocalDate): Long {
        getByDate(date)?.let { return it.id }
        return insert(DayEntity(date = date))
    }
}

@Dao
interface WorkoutDao {
    @Insert
    suspend fun insert(workout: WorkoutEntity): Long

    @Query("SELECT * FROM workouts WHERE dayId = :dayId ORDER BY createdAt")
    fun observeByDay(dayId: Long): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE dayId = :dayId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestByDay(dayId: Long): WorkoutEntity?
}

@Dao
interface SetDao {
    @Insert
    suspend fun insert(set: SetEntity): Long

    @Update
    suspend fun update(set: SetEntity)

    @Query("SELECT * FROM sets WHERE workoutId IN (:workoutIds) ORDER BY orderInWorkout")
    fun observeByWorkouts(workoutIds: List<Long>): Flow<List<SetEntity>>

    @Query(
        """
        SELECT COALESCE(MAX(weightKg), 0) FROM sets
        WHERE exerciseId = :exerciseId
        """
    )
    suspend fun getMaxWeight(exerciseId: Long): Float
}