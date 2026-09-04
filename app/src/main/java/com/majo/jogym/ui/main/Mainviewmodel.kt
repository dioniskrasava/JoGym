package com.majo.jogym.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.majo.jogym.data.dao.DayDao
import com.majo.jogym.data.dao.ExerciseDao
import com.majo.jogym.data.dao.SetDao
import com.majo.jogym.data.dao.WorkoutDao
import com.majo.jogym.data.model.SetWithExercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MainUiState(
    val date: LocalDate = LocalDate.now(),
    val sets: List<SetWithExercise> = emptyList(),
    val isLoading: Boolean = true
)

/**
 * В реальном проекте DAO стоит спрятать за репозиторием (WorkoutRepository),
 * здесь для наглядности прототипа обращаемся к ним напрямую.
 */
class MainViewModel(
    private val dayDao: DayDao,
    private val workoutDao: WorkoutDao,
    private val setDao: SetDao,
    private val exerciseDao: ExerciseDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    fun onDateChanged(date: LocalDate) {
        if (_uiState.value.date == date && !_uiState.value.isLoading) return
        _uiState.value = MainUiState(date = date, isLoading = true)

        viewModelScope.launch {
            val dayId = dayDao.getOrCreate(date)

            workoutDao.observeByDay(dayId)
                .flatMapLatest { workouts ->
                    val workoutIds = workouts.map { it.id }
                    if (workoutIds.isEmpty()) {
                        kotlinx.coroutines.flow.flowOf(emptyList())
                    } else {
                        combine(
                            setDao.observeByWorkouts(workoutIds),
                            exerciseDao.observeAll()
                        ) { sets, exercises ->
                            val exerciseById = exercises.associateBy { it.id }
                            sets.mapNotNull { set ->
                                exerciseById[set.exerciseId]?.let { exercise ->
                                    SetWithExercise(set, exercise)
                                }
                            }
                        }
                    }
                }
                .onEach { sets ->
                    _uiState.value = MainUiState(date = date, sets = sets, isLoading = false)
                }
                .launchIn(viewModelScope)
        }
    }
}