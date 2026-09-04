package com.majo.jogym.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.majo.jogym.data.model.ExerciseType
import com.majo.jogym.data.model.SetWithExercise
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * PAGE_COUNT / CENTER_PAGE — трюк для "бесконечного" горизонтального свайпа по дням.
 * Страница CENTER_PAGE соответствует сегодняшнему дню, страницы левее/правее — дни в прошлом/будущем.
 */
private const val CENTER_PAGE = 5000
private const val PAGE_COUNT = CENTER_PAGE * 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onOpenCalendar: () -> Unit,
    onAddSet: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = CENTER_PAGE) { PAGE_COUNT }
    val currentDate = LocalDate.now().plusDays((pagerState.currentPage - CENTER_PAGE).toLong())

    LaunchedEffect(currentDate) {
        viewModel.onDateChanged(currentDate)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentDate.formatForTopBar(),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = onOpenCalendar) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Календарь")
                    }
                    IconButton(onClick = onAddSet) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить подход")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Настройки")
                    }
                }
            )
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { page ->
            val date = LocalDate.now().plusDays((page - CENTER_PAGE).toLong())
            DayContent(date = date, viewModel = viewModel)
        }
    }
}

@Composable
private fun DayContent(date: LocalDate, viewModel: MainViewModel) {
    // В проде — по одному StateFlow на дату через ключ, здесь для прототипа
    // упрощенно дергаем текущее состояние вьюмодели (актуально для отображаемой страницы).
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.date != date) {
        // Страница еще не активна / данные не подгружены — просто пусто.
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (uiState.sets.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Тренировок за этот день нет.\nНажми + чтобы добавить подход.")
        }
        return
    }

    val grouped = uiState.sets.groupBy { it.exercise }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(grouped.entries.toList()) { (exercise, sets) ->
            ExerciseGroupCard(exerciseName = exercise.name, sets = sets)
        }
    }
}

@Composable
private fun ExerciseGroupCard(exerciseName: String, sets: List<SetWithExercise>) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(exerciseName, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            sets.forEachIndexed { index, item ->
                Text(
                    text = "${index + 1}. ${item.set.formatForDisplay(item.exercise.type)}" +
                            if (item.set.isPersonalRecord) " 🏆" else "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun com.majo.jogym.data.model.SetEntity.formatForDisplay(type: ExerciseType): String = when (type) {
    ExerciseType.WEIGHT_REPS -> "${weightKg ?: 0} кг × ${reps ?: 0}"
    ExerciseType.WEIGHT_TIME -> "${weightKg ?: 0} кг, ${timeSeconds ?: 0} сек"
    ExerciseType.DISTANCE_TIME -> "${distanceMeters ?: 0} м за ${timeSeconds ?: 0} сек"
    ExerciseType.TIME -> "${timeSeconds ?: 0} сек"
}

private fun LocalDate.formatForTopBar(): String {
    val today = LocalDate.now()
    return when (this) {
        today -> "Сегодня"
        today.minusDays(1) -> "Вчера"
        today.plusDays(1) -> "Завтра"
        else -> format(DateTimeFormatter.ofPattern("d MMM", Locale("ru")))
            .replaceFirstChar { it.uppercase() } +
                ", " + dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ru"))
    }
}