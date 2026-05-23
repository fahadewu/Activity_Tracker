package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ActivityEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityTrackerScreen(
    viewModel: ActivityViewModel,
    modifier: Modifier = Modifier
) {
    val selectedEpochDay by viewModel.selectedEpochDay.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val filteredActivities by viewModel.filteredActivities.collectAsStateWithLifecycle()
    val stats by viewModel.statsForSelectedDay.collectAsStateWithLifecycle()
    val allHistory by viewModel.allHistory.collectAsStateWithLifecycle()
    val categories = viewModel.categories

    // Controls active tab in the bottom bar
    var activeBottomTab by remember { mutableStateOf("Daily") }

    // Controls visibility of "Add Activity" dialog
    var showAddDialog by remember { mutableStateOf(false) }

    // Convert epoch day to formatted date
    val localDate = remember(selectedEpochDay) { LocalDate.ofEpochDay(selectedEpochDay) }
    val isToday = remember(selectedEpochDay) { localDate == LocalDate.now() }
    val formattedDate = remember(localDate) {
        if (localDate == LocalDate.now()) "Today"
        else if (localDate == LocalDate.now().minusDays(1)) "Yesterday"
        else localDate.format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy"))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                tonalElevation = 4.dp
            ) {
                NavigationBarItem(
                    selected = activeBottomTab == "Daily",
                    onClick = { activeBottomTab = "Daily" },
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Daily Logs") },
                    label = { Text("Daily", fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = activeBottomTab == "Stats",
                    onClick = { activeBottomTab = "Stats" },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Analytics") },
                    label = { Text("Stats", fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = activeBottomTab == "Settings",
                    onClick = { activeBottomTab = "Settings" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "System") },
                    label = { Text("Settings", fontWeight = FontWeight.Bold) }
                )
            }
        },
        floatingActionButton = {
            if (activeBottomTab == "Daily") {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("add_activity_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Activity",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 👤 PERSONALIZED GREETINGS UPPER BOARD (Geometric Balance aesthetic)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val hour = remember { java.time.LocalTime.now().hour }
                        val greeting = when (hour) {
                            in 5..11 -> "Good Morning,"
                            in 12..17 -> "Good Afternoon,"
                            else -> "Good Evening,"
                        }
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Fahad",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = (-0.5).sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👤",
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Screen Tab Routing
            AnimatedContent(
                targetState = activeBottomTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                modifier = Modifier.weight(1f),
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    "Daily" -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // 1. DATE SELECTOR HEADER CARD
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { viewModel.changeDay(-1) },
                                        modifier = Modifier.testTag("prev_day_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Previous Day"
                                        )
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = formattedDate,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (!isToday) {
                                            Text(
                                                text = "Reset to Today",
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .padding(top = 2.dp)
                                                    .clickable { viewModel.selectDate(LocalDate.now().toEpochDay()) }
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.changeDay(1) },
                                        modifier = Modifier.testTag("next_day_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Next Day"
                                        )
                                    }
                                }
                            }

                            // 2. DAILY PROGRESS DASHBOARD CARD (Geometric Balance)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1.5f)) {
                                        Text(
                                            text = "DAILY PROGRESS",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            letterSpacing = 1.2.sp
                                        )
                                        
                                        val targetMinutes = 120
                                        val percent = remember(stats.totalMinutes) {
                                            val p = (stats.totalMinutes.toDouble() / targetMinutes * 100).toInt()
                                            p.coerceIn(0, 100)
                                        }
                                        
                                        Text(
                                            text = "$percent%",
                                            fontSize = 38.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            lineHeight = 44.sp
                                        )
                                        
                                        Text(
                                            text = "${stats.activityCount} of required goals done today",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                    
                                    // Circular SVG style progress radial
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.size(80.dp)
                                    ) {
                                        val targetMinutes = 120
                                        val scaleFraction = remember(stats.totalMinutes) {
                                            (stats.totalMinutes.toFloat() / targetMinutes).coerceIn(0f, 1f)
                                        }
                                        val strokeWidth = 5.dp
                                        val primaryColor = MaterialTheme.colorScheme.primary
                                        val trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)

                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            drawArc(
                                                color = trackColor,
                                                startAngle = -90f,
                                                sweepAngle = 360f,
                                                useCenter = false,
                                                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                                            )
                                            drawArc(
                                                color = primaryColor,
                                                startAngle = -90f,
                                                sweepAngle = scaleFraction * 360f,
                                                useCenter = false,
                                                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                                            )
                                        }
                                        
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            // 2b. METRIC HIGHLIGHT PILLS
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val energyRating = if (stats.averageEnergy > 0.0) {
                                    String.format("%.1f/5", stats.averageEnergy)
                                } else "N/A"
                                
                                val dominantCategory = remember(stats.categoryBreakdown) {
                                    stats.categoryBreakdown.maxByOrNull { it.value }?.key ?: "None"
                                }

                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        .padding(vertical = 8.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Avg Energy: $energyRating",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        .padding(vertical = 8.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Top tag: $dominantCategory",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // 3. CATEGORY FILTER ROWS
                            Text(
                                text = "Pending Activities",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )

                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(categories) { category ->
                                    val isSelected = category == selectedFilter
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.selectFilter(category) },
                                        label = { Text(text = category, fontWeight = FontWeight.Medium) },
                                        modifier = Modifier.testTag("filter_chip_$category"),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }

                            // 4. MAIN HEALTHY ACTIVITIES LIST
                            if (filteredActivities.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Task,
                                            contentDescription = "Empty activity indicator",
                                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                            modifier = Modifier.size(56.dp)
                                        )
                                        Text(
                                            text = if (selectedFilter == "All") "No activities tracked on this day." else "No activities in '$selectedFilter'",
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Button(
                                            onClick = { showAddDialog = true },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Text("Track New Log")
                                        }
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(
                                        items = filteredActivities,
                                        key = { it.id }
                                    ) { activity ->
                                        ActivityItemCard(
                                            activity = activity,
                                            onDelete = { viewModel.deleteActivity(activity) },
                                            modifier = Modifier.testTag("activity_item_card_${activity.id}")
                                        )
                                    }
                                }
                            }
                        }
                    }

                    "Stats" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .background(MaterialTheme.colorScheme.background),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Analytics Dashboard",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Lifetime tracking statistics card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = "LIFETIME METRICS",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.2.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "${allHistory.size}",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(text = "Total Logs", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Column {
                                            Text(
                                                text = "${allHistory.sumOf { it.durationMinutes }} m",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(text = "Active Minutes", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Column {
                                            val avgLifeEnergy = if (allHistory.isNotEmpty()) {
                                                String.format("%.1f", allHistory.map { it.energyLevel }.average())
                                            } else "N/A"
                                            Text(
                                                text = avgLifeEnergy,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(text = "Avg Energy Status", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }

                            // Dynamic Distribution statistics inside Categories
                            Text(
                                text = "Category Time Distribution",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (allHistory.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Log activities to unlock distribution chart.")
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val groupedBreakdown = allHistory.groupBy { it.category }
                                        .mapValues { it.value.sumOf { item -> item.durationMinutes } }
                                    val maxDuration = groupedBreakdown.values.maxOrNull()?.toFloat() ?: 1f

                                    groupedBreakdown.forEach { (cat, mins) ->
                                        item {
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(text = cat, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                    Text(text = "$mins mins", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                LinearProgressIndicator(
                                                    progress = { mins.toFloat() / maxDuration },
                                                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Settings" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Security",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = "Profile & App Configurations",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Configure goals, theme settings and secure data synchronization.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("User Pro Level", fontWeight = FontWeight.Bold)
                                        Text("Active", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Assigned Account")
                                        Text("fahad.wp07@gmail.com", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Local DB Version")
                                        Text("v1 SQLite Local Room", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 5. ADD ACTIVITY DIALOG
    if (showAddDialog) {
        AddActivityDialog(
            categories = viewModel.defaultLogCategories,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, category, duration, notes, energy ->
                viewModel.addActivity(title, category, duration, notes, energy)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ActivityItemCard(
    activity: ActivityEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryIcon = when (activity.category) {
        "Fitness" -> Icons.Default.FitnessCenter
        "Work" -> Icons.Default.Computer
        "Study" -> Icons.Default.Book
        "Leisure" -> Icons.Default.LocalPlay
        "Nutrition" -> Icons.Default.Restaurant
        "Chores" -> Icons.Default.CleaningServices
        else -> Icons.Default.Star
    }

    // High quality soft container coloring adhering to HTML direction
    val iconContainerColor = when (activity.category) {
        "Fitness" -> Color(0xFFE2F3E5)
        "Work" -> Color(0xFFE3F2FD)
        "Study" -> Color(0xFFEDE7F6)
        "Leisure" -> Color(0xFFFFF3E0)
        "Nutrition" -> Color(0xFFFBE9E7)
        "Chores" -> Color(0xFFE8F5E9)
        else -> Color(0xFFF3EDF7)
    }

    val iconTint = when (activity.category) {
        "Fitness" -> Color(0xFF2E7D32)
        "Work" -> Color(0xFF1565C0)
        "Study" -> Color(0xFF651FFF)
        "Leisure" -> Color(0xFFE65100)
        "Nutrition" -> Color(0xFFD84315)
        "Chores" -> Color(0xFF4CAF50)
        else -> Color(0xFF6750A4)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category Icon Block as modern square
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = activity.category,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Main Info Block
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = activity.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${activity.durationMinutes} min",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Category and Energy status check labels
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = activity.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${activity.energyLevel}/5",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Description Notes, if present
                if (activity.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activity.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Beautiful status visual tick box mimicking Design HTML checklist style
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Logged Done",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Simple delete trigger
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_activity_button").size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Log",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, duration: Int, notes: String, energyLevel: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "Other") }
    var durationText by remember { mutableStateOf("30") }
    var notes by remember { mutableStateOf("") }
    var energyLevel by remember { mutableStateOf(3f) }

    var isDropdownExpanded by remember { mutableStateOf(false) }
    var triggerError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Track Activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        triggerError = false
                    },
                    label = { Text("Activity Title (e.g. Yoga, Code)") },
                    placeholder = { Text("Enter activity title") },
                    singleLine = true,
                    isError = triggerError && title.isBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("activity_title_input")
                )

                // Category Selection Dropdown
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(text = category) },
                                onClick = {
                                    selectedCategory = category
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Duration Input with quick preset bubbles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = {
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                durationText = it
                            }
                        },
                        label = { Text("Duration (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("activity_duration_input")
                    )

                    Row(
                        modifier = Modifier.weight(1.5f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("15", "30", "45", "60").forEach { preset ->
                            SuggestionChip(
                                onClick = { durationText = preset },
                                label = { Text(preset, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Energy Status Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Energy Status Rating",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "${energyLevel.toInt()}/5",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = energyLevel,
                        onValueChange = { energyLevel = it },
                        valueRange = 1f..5f,
                        steps = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Notes Optional Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Details / Notes (Optional)") },
                    placeholder = { Text("Add descriptive notes...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // Dialog Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                triggerError = true
                            } else {
                                val duration = durationText.toIntOrNull() ?: 0
                                onConfirm(
                                    title,
                                    selectedCategory,
                                    duration.coerceAtLeast(1),
                                    notes,
                                    energyLevel.toInt()
                                )
                            }
                        },
                        modifier = Modifier.testTag("save_activity_button")
                    ) {
                        Text("Save Log")
                    }
                }
            }
        }
    }
}
