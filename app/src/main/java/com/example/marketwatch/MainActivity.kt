package com.example.marketwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.marketwatch.di.appModule
import com.example.marketwatch.di.networkModule
import com.example.marketwatch.ui.screens.DetailScreen
import com.example.marketwatch.ui.screens.FavoritesScreen
import com.example.marketwatch.ui.screens.SearchScreen
import com.example.marketwatch.ui.screens.SettingsScreen
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import androidx.navigation.compose.*
import com.example.marketwatch.data.local.SettingsDataStore
import com.example.marketwatch.data.repository.SettingsRepository
import com.example.marketwatch.presentaion.MVI.SearchViewModel
import com.example.marketwatch.ui.theme.LanguageProvider
import com.example.marketwatch.ui.screens.Screen
import com.example.marketwatch.ui.theme.stringResourceWithLocale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
   // private lateinit var settingsRepo: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

     //   settingsRepo = SettingsRepository(this)

      //  startKoin {
      //      androidContext(this@MainActivity)
      //      modules(networkModule, appModule)
        //}

     //   lifecycleScope.launch(Dispatchers.IO) {
     //       settingsRepo.scheduleRefresh()
     //   }

        setContent {
            val settingsDataStore = koinInject<SettingsDataStore>()
            val currentLanguage by settingsDataStore.languageFlow.collectAsState()
            val isDark by settingsDataStore.isDarkModeStateFlow.collectAsState()
            //val currentLanguage by settingsRepo.languageFlow.collectAsState()
           // val isDark by settingsRepo.isDarkModeFlow.collectAsState()


            AppTheme(darkTheme = isDark) {
                LanguageProvider(language = currentLanguage) {
                    Surface {
                        MarketWatchNavHost()
                    }
                }
            }
        }
    }

    @Composable
    fun MarketWatchNavHost() {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val snackbarHostState = remember { SnackbarHostState() }
        val searchViewModel: SearchViewModel = koinViewModel() 

        LaunchedEffect(searchViewModel.snackbarMessage) {
            searchViewModel.snackbarMessage.collectLatest { message ->
                message?.let {
                    val result = snackbarHostState.showSnackbar(it)
                    when (result) {
                        SnackbarResult.ActionPerformed -> {
                            println("Snackbar action clicked")
                        }

                        SnackbarResult.Dismissed -> {
                            searchViewModel.clearSnackbar()
                        }
                    }
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Search, contentDescription = null) },
                        label = { Text(stringResourceWithLocale(R.string.search)) },
                        selected = currentRoute == Screen.Search.route,
                        onClick = { navController.navigate(Screen.Search.route) { popUpTo(0) } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                        label = { Text(stringResourceWithLocale(R.string.favorites)) }, 
                        selected = currentRoute == Screen.Favorites.route,
                        onClick = { navController.navigate(Screen.Favorites.route) { popUpTo(0) } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text(stringResourceWithLocale(R.string.settings)) }, 
                        selected = currentRoute == Screen.Settings.route,
                        onClick = { navController.navigate(Screen.Settings.route) { popUpTo(0) } }
                    )
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Search.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(Screen.Search.route) {
                    SearchScreen(navController)
                }
                composable(Screen.Favorites.route) {
                    FavoritesScreen(navController)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen()
                }
                composable(
                    "detail/{symbol}",
                    arguments = listOf(navArgument("symbol") {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
                    DetailScreen(symbol, navController)
                }
            }
        }
    }

    @Composable
    fun AppTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        content: @Composable () -> Unit
    ) {
        val colorScheme = when {
            darkTheme -> darkColorScheme()
            else -> lightColorScheme()
        }

        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}







