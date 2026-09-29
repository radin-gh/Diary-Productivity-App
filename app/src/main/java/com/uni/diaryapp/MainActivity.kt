package com.uni.diaryapp

import ToDoScreen
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager

import com.uni.diaryapp.data.model.DiaryEntry
import com.uni.diaryapp.notifications.createNotificationChannel
import com.uni.diaryapp.receiver.DailyTodoScheduler
import com.uni.diaryapp.receiver.DailyTodoWorker
import com.uni.diaryapp.ui.auth.AuthViewModel
import com.uni.diaryapp.ui.auth.LoginScreen
import com.uni.diaryapp.ui.auth.RegisterScreen
import com.uni.diaryapp.ui.calendar.CalendarScreen
import com.uni.diaryapp.ui.diary.DiaryArchiveScreen
import com.uni.diaryapp.ui.diary.DiaryScreen
import com.uni.diaryapp.ui.diary.DiaryViewModel
import com.uni.diaryapp.ui.diary.WriteDiaryScreen

import com.uni.diaryapp.ui.home.HomeScreen

import com.uni.diaryapp.ui.todo.ToDoViewModel
import com.uni.diaryapp.ui.todo.ToDoArchiveScreen

import com.uni.test.ui.theme.DiaryAppTheme
import java.time.LocalDate
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {

            DiaryAppTheme {
                val navController = rememberNavController()
                val diaryViewModel: DiaryViewModel = viewModel()
                val todoViewModel: ToDoViewModel = viewModel()
                val authViewModel: AuthViewModel = viewModel()
                val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

                Scaffold(
                    bottomBar = {
                        NavigationBar() {
                            NavigationBarItem(
                                selected = navController.currentBackStackEntryAsState().value?.destination?.route == "home",
                                onClick = { navController.navigate("home") { launchSingleTop = true } },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Home") }
                            )
                            NavigationBarItem(
                                selected = navController.currentBackStackEntryAsState().value?.destination?.route == "calendar",
                                onClick = { navController.navigate("calendar") { launchSingleTop = true } },
                                icon = { Icon(Icons.Default.DateRange, contentDescription = "Calendar") },
                                label = { Text("Calendar") }
                            )
                            NavigationBarItem(
                                selected = navController.currentBackStackEntryAsState().value?.destination?.route == "diaryArchive",
                                onClick = { navController.navigate("diaryArchive") { launchSingleTop = true } },
                                icon = { Icon(Icons.Default.Create, contentDescription = "Diary Archive") },
                                label = { Text("Diary") }
                            )
                            NavigationBarItem(
                                selected = navController.currentBackStackEntryAsState().value?.destination?.route == "todoArchive",
                                onClick = { navController.navigate("todoArchive") { launchSingleTop = true } },
                                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Todo Archive") },
                                label = { Text("Todos") }
                            )
                        }
                    }
                )
                { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "auth",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("auth") {
                            if (isAuthenticated) {

                                LaunchedEffect(Unit) {
                                    navController.navigate("home") {
                                        popUpTo("auth") {
                                            inclusive = true
                                        }
                                    }
                                }

                            } else {
                                LoginScreen(
                                    viewModel = authViewModel,
                                    onLoginSuccess = {
                                        navController.navigate("home") {
                                            popUpTo("auth") {
                                                inclusive = true
                                            }
                                        }
                                    },
                                    onRegisterClick = {
                                        navController.navigate("register")
                                    }
                                )
                            }
                        }

                        composable("register") {
                            RegisterScreen(
                                viewModel = authViewModel,
                                onRegisterSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("register") {
                                            inclusive = true
                                        }
                                    }
                                },
                                onLoginClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("home") { HomeScreen(diaryViewModel = diaryViewModel, todoViewModel = todoViewModel,onNavigateToDiary = { entry ->
                            navController.navigate("diary/${entry.id}")}, )}
                        composable("calendar") { CalendarScreen(diaryViewModel = diaryViewModel, onAddDiary = { date -> navController.navigate("writeDiary/${date}") }, onAddTodo = { date -> navController.navigate("todos/${date}") }) }
                        composable("diaryArchive") { DiaryArchiveScreen(diaryViewModel = diaryViewModel, onDiaryClicked = { entry -> navController.navigate("diary/${entry.id}") }) }
                        composable("todoArchive") { ToDoArchiveScreen(todoViewModel = todoViewModel) }
                        composable("writeDiary/{date}", arguments = listOf(navArgument("date") { type = NavType.StringType })) { backStackEntry ->
                            val date = backStackEntry.arguments?.getString("date")?.let { LocalDate.parse(it) } ?: LocalDate.now()
                            WriteDiaryScreen(diaryViewModel = diaryViewModel, selectedDate = date, onBack = { navController.popBackStack() })
                        }
                        composable("diary/{diaryId}", arguments = listOf(navArgument("diaryId") { type = NavType.IntType })) { backStackEntry ->
                            val diaryId = backStackEntry.arguments?.getInt("diaryId")
                            val diaryList by diaryViewModel.diaryList.collectAsState()
                            diaryList.firstOrNull { it.id == diaryId }?.let { DiaryScreen(diaryEntry = it) }
                        }
                        composable("todos/{date}") { backStackEntry ->
                            val date = backStackEntry.arguments?.getString("date")?.let { LocalDate.parse(it) }
                            ToDoScreen(isFromCalendar = true, selectedDate = date)
                        }

                    }
                }
            }
        }
        DailyTodoScheduler.scheduleDailyTodoWorker(this)

        // Request notification permissions for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        createNotificationChannel(this)
        val testWorkRequest = OneTimeWorkRequestBuilder<DailyTodoWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS) // triggers 5 seconds after app start
            .build()

        WorkManager.getInstance(this).enqueue(testWorkRequest)
    }







    /*private fun scheduleDailyTodoWorker() {
        val workRequest = PeriodicWorkRequestBuilder<DailyTodoWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_todo_worker",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }*/

    // Calculate delay until next 9 AM (or any hour you want)
    /*private fun calculateInitialDelay(): Long {
        val now = java.util.Calendar.getInstance()
        val next = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 9)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            if (before(now)) add(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        return next.timeInMillis - now.timeInMillis
    }*/

}

