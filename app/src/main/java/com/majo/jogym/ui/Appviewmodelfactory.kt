package com.majo.jogym.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.majo.jogym.data.AppDatabase
import com.majo.jogym.ui.exercisepicker.ExercisePickerViewModel
import com.majo.jogym.ui.main.MainViewModel

class MainViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(
            dayDao = db.dayDao(),
            workoutDao = db.workoutDao(),
            setDao = db.setDao(),
            exerciseDao = db.exerciseDao()
        ) as T
    }
}

class ExercisePickerViewModelFactory(
    private val db: AppDatabase,
    private val workoutId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ExercisePickerViewModel(
            exerciseDao = db.exerciseDao(),
            setDao = db.setDao(),
            currentWorkoutId = workoutId
        ) as T
    }
}