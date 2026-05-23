package com.example.data

import kotlinx.coroutines.flow.Flow

class ActivityRepository(private val activityDao: ActivityDao) {
    fun getAllActivities(): Flow<List<ActivityEntity>> = activityDao.getAllActivities()

    fun getActivitiesForDay(epochDay: Long): Flow<List<ActivityEntity>> =
        activityDao.getActivitiesForDay(epochDay)

    suspend fun insertActivity(activity: ActivityEntity) =
        activityDao.insertActivity(activity)

    suspend fun updateActivity(activity: ActivityEntity) =
        activityDao.updateActivity(activity)

    suspend fun deleteActivity(activity: ActivityEntity) =
        activityDao.deleteActivity(activity)

    suspend fun deleteActivityById(id: Long) =
        activityDao.deleteActivityById(id)
}
