package com.majo.jogym.ui.exercisepicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.majo.jogym.data.dao.ExerciseDao
import com.majo.jogym.data.dao.SetDao
import com.majo.jogym.data.model.ExerciseEntity
import com.majo.jogym.data.model.MuscleGroup
import com.majo.jogym.data.model.SetEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class ExercisePickerUiState(
    val query: String = "",
    val grouped: Map<MuscleGroup, List<ExerciseEntity>> = emptyMap()
)

/**
 * currentWorkoutId нужен, чтобы знать, куда сохранять новый подход.
 * В реальном флоу его создает MainViewModel/навигация при переходе "добавить подход".
 */
class ExercisePickerViewModel(
    private val exerciseDao: ExerciseDao,
    private val setDao: SetDao,
    private val currentWorkoutId: Long
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _uiState = MutableStateFlow(ExercisePickerUiState())
    val uiState: StateFlow<ExercisePickerUiState> = _uiState

    init {
        combine(_query, exerciseDao.observeAll()) { query, exercises ->
            val filtered = if (query.isBlank()) {
                exercises
            } else {
                exercises.filter { it.name.contains(query, ignoreCase = true) }
            }
            filtered.groupBy { it.muscleGroup }
        }.onEach { grouped ->
            _uiState.value = _uiState.value.copy(grouped = grouped)
        }.launchIn(viewModelScope)
    }

    fun onQueryChanged(value: String) {
        _query.value = value
        _uiState.value = _uiState.value.copy(query = value)
    }

    fun saveSet(
        exercise: ExerciseEntity,
        weight: Float?,
        reps: Int?,
        time: Int?,
        distance: Float?
    ) {
        viewModelScope.launch {
            val maxWeight = setDao.getMaxWeight(exercise.id)
            val isRecord = (weight ?: 0f) > maxWeight

            setDao.insert(
                SetEntity(
                    workoutId = currentWorkoutId,
                    exerciseId = exercise.id,
                    orderInWorkout = 0, // TODO: считать реальный порядковый номер в тренировке
                    weightKg = weight,
                    reps = reps,
                    timeSeconds = time,
                    distanceMeters = distance,
                    isPersonalRecord = isRecord
                )
            )
        }
    }
}