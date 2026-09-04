package com.majo.jogym.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

// ---------- Enums ----------

enum class MuscleGroup(val displayName: String) {
    CHEST("Грудь"),
    BACK("Спина"),
    LEGS("Ноги"),
    SHOULDERS("Плечи"),
    BICEPS("Бицепс"),
    TRICEPS("Трицепс"),
    CORE("Пресс"),
    FULL_BODY("Все тело"),
    CARDIO("Кардио")
}

/**
 * Тип упражнения определяет, какие поля показывать при вводе подхода.
 */
enum class ExerciseType(val displayName: String) {
    WEIGHT_REPS("Масса и повторения"),   // жим, присед и т.п.
    WEIGHT_TIME("Масса и время"),        // удержание снаряда (планка с блином, статика)
    DISTANCE_TIME("Расстояние и время"), // бег, гребля
    TIME("Время")                        // планка, вис
}

// ---------- Entities ----------

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val muscleGroup: MuscleGroup,
    val type: ExerciseType
)

@Entity(tableName = "days")
data class DayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate
)

@Entity(
    tableName = "workouts",
    foreignKeys = [
        ForeignKey(
            entity = DayEntity::class,
            parentColumns = ["id"],
            childColumns = ["dayId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("dayId")]
)
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayId: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("workoutId"), Index("exerciseId")]
)
data class SetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,
    val exerciseId: Long,
    val orderInWorkout: Int,
    // Заполняются в зависимости от ExerciseType — остальные остаются null.
    val weightKg: Float? = null,
    val reps: Int? = null,
    val timeSeconds: Int? = null,
    val distanceMeters: Float? = null,
    val isPersonalRecord: Boolean = false
)

/**
 * Проекция для главного экрана: подход + данные упражнения одним объектом,
 * чтобы не тащить джойны в UI-слой.
 */
data class SetWithExercise(
    val set: SetEntity,
    val exercise: ExerciseEntity
)