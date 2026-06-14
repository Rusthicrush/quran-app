package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.api.QuranApiService
import com.example.data.db.AppDatabase
import com.example.data.repository.QuranRepository
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SurahDetailScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.QuranViewModelFactory
import com.example.ui.viewmodel.SurahDetailViewModel
import com.example.ui.viewmodel.SurahDetailViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Core Local SQLite Database (Room) & API Interfaces (Retrofit)
        val database = AppDatabase.getDatabase(applicationContext)
        val bookmarkDao = database.bookmarkDao()
        val apiService = QuranApiService.create()
        val repository = QuranRepository(apiService, bookmarkDao)

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()

                // Create main ViewModel via constructor factory
                val homeViewModel: QuranViewModel = viewModel(
                    factory = QuranViewModelFactory(repository)
                )

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.fillMaxSize().padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(
                                viewModel = homeViewModel,
                                onSurahClick = { number, englishName, arabicName ->
                                    val encEng = Uri.encode(englishName)
                                    val encAra = Uri.encode(arabicName)
                                    navController.navigate("detail/$number/$encEng/$encAra")
                                },
                                onAboutClick = {
                                    navController.navigate("about")
                                }
                            )
                        }
                        composable("about") {
                            AboutScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "detail/{number}/{englishName}/{arabicName}",
                            arguments = listOf(
                                navArgument("number") { type = NavType.IntType },
                                navArgument("englishName") { type = NavType.StringType },
                                navArgument("arabicName") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val surahNumber = backStackEntry.arguments?.getInt("number") ?: 1
                            val rawEng = backStackEntry.arguments?.getString("englishName") ?: ""
                            val rawAra = backStackEntry.arguments?.getString("arabicName") ?: ""
                            val decEng = Uri.decode(rawEng)
                            val decAra = Uri.decode(rawAra)

                            // Surah specific details ViewModel with dedicated factory
                            val detailViewModel: SurahDetailViewModel = viewModel(
                                factory = SurahDetailViewModelFactory(
                                    repository = repository,
                                    surahNumber = surahNumber,
                                    surahEnglishName = decEng,
                                    surahArabicName = decAra
                                ),
                                key = "surah_detail_$surahNumber"
                            )

                            SurahDetailScreen(
                                viewModel = detailViewModel,
                                onBackClick = { navController.popBackStack() },
                                surahNumber = surahNumber,
                                surahEnglishName = decEng,
                                surahArabicName = decAra
                            )
                        }
                    }
                }
            }
        }
    }
}

