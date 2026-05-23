package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.ActivityRepository
import com.example.ui.ActivityViewModel
import com.example.ui.ActivityTrackerScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ActivityRepository(database.activityDao())

        setContent {
            MyApplicationTheme {
                val viewModel: ActivityViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = ActivityViewModel.Factory(repository)
                )
                
                ActivityTrackerScreen(viewModel = viewModel)
            }
        }
    }
}
