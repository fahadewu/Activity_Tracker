package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // e.g., "Work", "Fitness", "Study", "Leisure", "Nutrition", "Chores", "Other"
    val durationMinutes: Int,
    val dateEpochDay: Long, // java.time.LocalDate.now().toEpochDay()
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val energyLevel: Int = 3 // 1 to 5 rating
)
