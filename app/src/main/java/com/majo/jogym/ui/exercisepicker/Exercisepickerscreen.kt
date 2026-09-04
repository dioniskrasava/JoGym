package com.majo.jogym.ui.exercisepicker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.majo.jogym.data.model.ExerciseEntity
import com.majo.jogym.data.model.ExerciseType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerScreen(
    viewModel: ExercisePickerViewModel,
    onBack: () -> Unit,
    onSetSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedExercise by remember { mutableStateOf<ExerciseEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Выбор упражнения") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Поиск упражнения") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                uiState.grouped.forEach { (group, exercises) ->
                    item {
                        Text(
                            text = group.displayName,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(exercises) { exercise ->
                        ListItem(
                            headlineContent = { Text(exercise.name) },
                            supportingContent = { Text(exercise.type.displayName) },
                            modifier = Modifier.clickableRow { selectedExercise = exercise }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    selectedExercise?.let { exercise ->
        AddSetSheet(
            exercise = exercise,
            onDismiss = { selectedExercise = null },
            onSave = { weight, reps, time, distance ->
                viewModel.saveSet(exercise, weight, reps, time, distance)
                selectedExercise = null
                onSetSaved()
            }
        )
    }
}

// Небольшой хелпер, чтобы не тащить combinedClickable ради простого onClick в прототипе.
private fun Modifier.clickableRow(onClick: () -> Unit): Modifier =
    this.then(Modifier.clickable(onClick = onClick))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSetSheet(
    exercise: ExerciseEntity,
    onDismiss: () -> Unit,
    onSave: (weight: Float?, reps: Int?, time: Int?, distance: Float?) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(24.dp)) {
            Text(exercise.name, style = MaterialTheme.typography.titleLarge)
            Text(exercise.type.displayName, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))

            // Поля ввода зависят от типа упражнения.
            when (exercise.type) {
                ExerciseType.WEIGHT_REPS -> {
                    NumberField("Вес, кг", weight) { weight = it }
                    NumberField("Повторения", reps) { reps = it }
                }
                ExerciseType.WEIGHT_TIME -> {
                    NumberField("Вес, кг", weight) { weight = it }
                    NumberField("Время, сек", time) { time = it }
                }
                ExerciseType.DISTANCE_TIME -> {
                    NumberField("Расстояние, м", distance) { distance = it }
                    NumberField("Время, сек", time) { time = it }
                }
                ExerciseType.TIME -> {
                    NumberField("Время, сек", time) { time = it }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    onSave(
                        weight.toFloatOrNull(),
                        reps.toIntOrNull(),
                        time.toIntOrNull(),
                        distance.toFloatOrNull()
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить подход")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun NumberField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}