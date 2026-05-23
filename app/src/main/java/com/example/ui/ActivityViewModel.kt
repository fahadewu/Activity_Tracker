package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ActivityEntity
import com.example.data.ActivityRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class ActivityViewModel(private val repository: ActivityRepository) : ViewModel() {

    // Selected Epoch Day - defaults to today
    private val _selectedEpochDay = MutableStateFlow(LocalDate.now().toEpochDay())
    val selectedEpochDay: StateFlow<Long> = _selectedEpochDay.asStateFlow()

    // Filter Category
    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    // All categories list
    val categories = listOf("All", "Fitness", "Work", "Study", "Leisure", "Nutrition", "Chores", "Other")
    val defaultLogCategories = listOf("Fitness", "Work", "Study", "Leisure", "Nutrition", "Chores", "Other")

    // Retrieve activities for the selected epoch day
    val activitiesForSelectedDay: StateFlow<List<ActivityEntity>> = _selectedEpochDay
        .flatMapLatest { epochDay ->
            repository.getActivitiesForDay(epochDay)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered activities based on selected category
    val filteredActivities: StateFlow<List<ActivityEntity>> = combine(
        activitiesForSelectedDay,
        _selectedFilter
    ) { list, category ->
        if (category == "All") list else list.filter { it.category == category }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Statistics calculated on the day's activities
    val statsForSelectedDay: StateFlow<DayStats> = activitiesForSelectedDay
        .map { list ->
            val totalMinutes = list.sumOf { it.durationMinutes }
            val count = list.size
            val avgEnergy = if (list.isNotEmpty()) list.map { it.energyLevel }.average() else 0.0
            
            // Map of categories and total duration
            val categoryDurations = list.groupBy { it.category }
                .mapValues { (_, activities) -> activities.sumOf { it.durationMinutes } }

            DayStats(
                totalMinutes = totalMinutes,
                activityCount = count,
                averageEnergy = avgEnergy,
                categoryBreakdown = categoryDurations
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DayStats()
        )

    // Historical activities flow to compile streak or total stats
    val allHistory: StateFlow<List<ActivityEntity>> = repository.getAllActivities()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun changeDay(offset: Long) {
        _selectedEpochDay.value = _selectedEpochDay.value + offset
    }

    fun selectDate(epochDay: Long) {
        _selectedEpochDay.value = epochDay
    }

    fun selectFilter(category: String) {
        _selectedFilter.value = category
    }

    fun addActivity(
        title: String,
        category: String,
        durationMinutes: Int,
        notes: String,
        energyLevel: Int
    ) {
        viewModelScope.launch {
            val activity = ActivityEntity(
                title = title.trim().ifEmpty { "Untitled Activity" },
                category = category,
                durationMinutes = durationMinutes.coerceAtLeast(1),
                dateEpochDay = _selectedEpochDay.value,
                notes = notes.trim(),
                energyLevel = energyLevel
            )
            repository.insertActivity(activity)
        }
    }

    fun deleteActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            repository.deleteActivity(activity)
        }
    }

    fun updateActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            repository.updateActivity(activity)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: ActivityRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ActivityViewModel::class.java)) {
                return ActivityViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class DayStats(
    val totalMinutes: Int = 0,
    val activityCount: Int = 0,
    val averageEnergy: Double = 0.0,
    val categoryBreakdown: Map<String, Int> = emptyMap()
)
