package com.majo.jogym.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.majo.jogym.data.AppDatabase
import com.majo.jogym.data.model.WorkoutEntity
import com.majo.jogym.ui.exercisepicker.ExercisePickerScreen
import com.majo.jogym.ui.exercisepicker.ExercisePickerViewModel
import com.majo.jogym.ui.main.MainScreen
import com.majo.jogym.ui.main.MainViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun JoGymApp() {
    val context = LocalContext.current

    // remember { } живет, пока жив JoGymApp (то есть пока не убита Activity) —
    // для прототипа этого достаточно, singleton через Application/DI сделаем позже.
    val db = remember {
        Room.databaseBuilder(context, AppDatabase::class.java, "jogym.db")
            // Миграций пока нет: при изменении схемы база будет пересоздана с нуля.
            .fallbackToDestructiveMigration()
            .build()
    }

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {

        composable("main") {
            val scope = rememberCoroutineScope()
            val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(db))

            MainScreen(
                viewModel = viewModel,
                onOpenCalendar = { /* TODO: экран календаря */ },
                onAddSet = {
                    scope.launch {
                        val workoutId = getOrCreateTodayWorkout(db)
                        navController.navigate("exercisePicker/$workoutId")
                    }
                },
                onOpenSettings = { /* TODO: экран настроек */ }
            )
        }

        composable(
            route = "exercisePicker/{workoutId}",
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getLong("workoutId") ?: return@composable
            val viewModel: ExercisePickerViewModel =
                viewModel(factory = ExercisePickerViewModelFactory(db, workoutId))

            ExercisePickerScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSetSaved = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Одна тренировка на день: если она уже есть — переиспользуем, иначе создаем.
 * Если тебе нужно несколько тренировок в один день (например, утро/вечер),
 * это место придется поменять — сейчас логика намеренно упрощена.
 */
private suspend fun getOrCreateTodayWorkout(db: AppDatabase): Long {
    val today = LocalDate.now()
    val dayId = db.dayDao().getOrCreate(today)
    db.workoutDao().getLatestByDay(dayId)?.let { return it.id }
    return db.workoutDao().insert(WorkoutEntity(dayId = dayId))
}