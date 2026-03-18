package com.example.marketwatch.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import com.example.marketwatch.R
import com.example.marketwatch.data.local.FavoriteEntity
import com.example.marketwatch.data.local.SettingsDataStore
import com.example.marketwatch.presentaion.viewmodels.InstrumentDetailsViewModel
import com.example.marketwatch.data.repository.SettingsRepository
import com.example.marketwatch.presentaion.viewmodels.FavoritesViewModel
import com.example.marketwatch.ui.theme.stringResourceWithLocale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * Навигация приложения (4 экрана).
 * Sealed class = типобезопасные маршруты + аргументы.
 */
sealed class Screen(val route: String) {
    object Search : Screen("search")
    object Favorites : Screen("favorites")
    object Settings : Screen("settings")
    object Detail : Screen("detail/{symbol}") {
        fun createRoute(symbol: String) = "detail/$symbol" // AAPL → detail/AAPL
    }
}

/**
 * ✅ FavoritesScreen - список избранных акций из Room БД.
 * Реактивный список (Flow) + кнопки Удалить/Детали.
 */
@Composable
fun FavoritesScreen(
    navController: NavHostController,
    viewModel: FavoritesViewModel = koinViewModel() // ✅ Koin DI
) {
    val favorites by viewModel.favorites.collectAsState() // ✅ Автообновление

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (favorites.isEmpty()) { // ✅ Пустое состояние
            item {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FavoriteBorder, null, Modifier.size(64.dp))
                        Text(
                            (stringResourceWithLocale(R.string.add_first_ticker)),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }
        } else {
            items(favorites) { favorite ->
                FavoriteItem(
                    favorite = favorite,
                    onDelete = { viewModel.deleteFavorite(favorite.symbol) }, //✅ Room delete
                    onDetails = { navController.navigate(Screen.Detail.createRoute(favorite.symbol)) }
                )
            }
        }
    }
}

/**
 * ✅ Карточка акции с кнопками действий.
 * Цена + изменение % + навигация.
 */
@Composable
fun FavoriteItem(favorite: FavoriteEntity, onDelete: () -> Unit, onDetails: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(favorite.symbol, style = MaterialTheme.typography.titleLarge)
                Text("$${String.format("%.2f", favorite.price)}") // ✅ Форматирование
                Text("${String.format("%.2f", favorite.changePercent)}%")

                //  Время обновления
                Text(
                    text = "${stringResourceWithLocale(R.string.updated)}: ${
                        SimpleDateFormat("HH:mm", Locale.getDefault())
                            .format(Date(favorite.lastUpdated))
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDetails) {
                Icon(Icons.Default.Info, stringResourceWithLocale(R.string.details))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, stringResourceWithLocale(R.string.delete))
            }
        }
    }
}

/**
 * ✅ Экран деталей акции (заглушка).
 * В будущем: график, полные котировки API.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(symbol: String, navController: NavHostController) {
    val viewModel: InstrumentDetailsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(symbol) {
        viewModel.loadInstrument(symbol)
    }

    // ✅ ВСЁ UI ВНУТРИ Box!
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1️⃣ Symbol
        Text(
            text = symbol,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 2️⃣ Цена
        Text(
            text = uiState.price,
            style = MaterialTheme.typography.headlineLarge,
            color = if (uiState.price.isNotEmpty())
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3️⃣ Изменения
        uiState.change?.let { change ->
            val color = if (change.startsWith("-"))
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.primary
            Text(
                text = change,
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        } ?: Text(
            text = stringResourceWithLocale(R.string.changes_unavailable),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4️⃣ Время
        Text(
            text = uiState.lastUpdateTime,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (uiState.isOffline) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResourceWithLocale(R.string.offline_cache),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 5️⃣ Кнопка Обновить
        Button(
            onClick = { viewModel.refreshInstrument() },
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResourceWithLocale(R.string.updating))
                }
            } else {
                Text(stringResourceWithLocale(R.string.refresh))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 6️⃣ Назад
        OutlinedButton(onClick = { navController.popBackStack() }) {
            Text(stringResourceWithLocale(R.string.back))
        }
    }  // ← Конец Column
}

/**
 * ✅ SettingsScreen - ВСЕ настройки в одном месте.
 * Реактивная темная тема + сохранение в SharedPreferences.
 */
@Composable
fun SettingsScreen() {  // ✅ navController убран
    val context = LocalContext.current
  //  val settingsRepo = remember { SettingsRepository(context) }
    val settingsDataStore = koinInject<SettingsDataStore>()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Заголовок
        item {
            Text(
                stringResourceWithLocale(R.string.settings_screen),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // 2. API Key
       // item { ApiKeySection(settingsRepo) }

        // 3. Темная тема
       // item { DarkModeSection(settingsRepo) }

        // 4. Автообновление
       // item { RefreshIntervalSection(settingsRepo) }

        // 5. Валюта
      //  item { CurrencySection(settingsRepo) }

        // 6. Язык ✅ НОВЫЙ!
       // item { LanguageSection(settingsRepo) }

        item { ApiKeySection(settingsDataStore) }
        item { DarkModeSection(settingsDataStore) }
        item { RefreshIntervalSection(settingsDataStore) }
        item { CurrencySection(settingsDataStore) }
        item { LanguageSection(settingsDataStore) }
    }
}

// ✅ Вынос в отдельные composables для читаемости
//@Composable
//private fun ApiKeySection(settingsRepo: SettingsRepository) {
//    var apiKey by remember { mutableStateOf(settingsRepo.apiKey) }
 //   Card(modifier = Modifier.fillMaxWidth()) {
 //       Column(Modifier.padding(16.dp)) {
 ///           Text(stringResourceWithLocale(R.string.api_key_title), style = MaterialTheme.typography.titleMedium)
 //           Spacer(modifier = Modifier.height(8.dp))
  //          OutlinedTextField(
  //              value = apiKey,
  //              onValueChange = {
  //                  apiKey = it
  //                  settingsRepo.apiKey = it
  //              },
   //             label = { Text((stringResourceWithLocale(R.string.api_key_hint)))},
   //             modifier = Modifier.fillMaxWidth(),
   //             singleLine = true
   //         )
  //      }
  //  }
//}

@Composable
private fun ApiKeySection(settingsDataStore: SettingsDataStore) {
    var apiKey by remember { mutableStateOf("") }

    // Загружаем значение
    LaunchedEffect(Unit) {
        apiKey = settingsDataStore.getApiKey()
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResourceWithLocale(R.string.api_key_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = apiKey,
                onValueChange = { newApiKey ->
                    apiKey = newApiKey
                    CoroutineScope(Dispatchers.IO).launch {
                        settingsDataStore.updateApiKey(newApiKey)
                    }
                },
                label = { Text(stringResourceWithLocale(R.string.api_key_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

//@Composable
//private fun DarkModeSection(settingsRepo: SettingsRepository) {
 //   Card(modifier = Modifier.fillMaxWidth()) {
  //      Column(Modifier.padding(16.dp)) {
   //         Text(stringResourceWithLocale(R.string.dark_theme_title), style = MaterialTheme.typography.titleMedium)
   //         Spacer(modifier = Modifier.height(8.dp))
   //         Row(
    //            modifier = Modifier.fillMaxWidth(),
    //            verticalAlignment = Alignment.CenterVertically
    //        ) {
    //            RadioButton(
    //                selected = !settingsRepo.isDarkMode,
    //                onClick = { settingsRepo.isDarkMode = false }
    //            )
     //           Text(stringResourceWithLocale(R.string.light))
     //           Spacer(modifier = Modifier.weight(1f))
     //           RadioButton(
     //               selected = settingsRepo.isDarkMode,
     //               onClick = { settingsRepo.isDarkMode = true }
      //          )
      //          Text(stringResourceWithLocale(R.string.dark))
     //       }
   //     }
  //  }
//}

@Composable
private fun DarkModeSection(settingsDataStore: SettingsDataStore) {
    val isDark by settingsDataStore.isDarkModeStateFlow.collectAsState()
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResourceWithLocale(R.string.dark_theme_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = !isDark,
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            settingsDataStore.updateDarkMode(false)
                        }
                    }
                )
                Text(stringResourceWithLocale(R.string.light))
                Spacer(modifier = Modifier.weight(1f))
                RadioButton(
                    selected = isDark,
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            settingsDataStore.updateDarkMode(true)
                        }
                    }
                )
                Text(stringResourceWithLocale(R.string.dark))
            }
        }
    }
}


//@Composable
//private fun RefreshIntervalSection(settingsRepo: SettingsRepository) {
 //   val intervals = listOf(
  //      stringResourceWithLocale(R.string.auto_refresh_title_hint) to 0,
   //     stringResourceWithLocale(R.string.fifteen_minutes) to 15,
   //     stringResourceWithLocale(R.string.sixty_minutes) to 60
   // )

 //   Card(modifier = Modifier.fillMaxWidth()) {
 //       Column(Modifier.padding(16.dp)) {
  //          Text(stringResourceWithLocale(R.string.auto_refresh_title), style = MaterialTheme.typography.titleMedium)
  //          Spacer(modifier = Modifier.height(8.dp))

  //          intervals.forEach { (label, value) ->
  //              Row(
   //                 modifier = Modifier
  //                      .fillMaxWidth()
   //                     .clickable {
  //                          settingsRepo.refreshInterval = value  // ✅ Автоматически вызывает scheduleRefresh()
  //                      }
   //                     .padding(vertical = 4.dp),
  //                  verticalAlignment = Alignment.CenterVertically
  //              ) {
   //                 RadioButton(
   //                     selected = settingsRepo.refreshInterval == value,
   //                     onClick = { settingsRepo.refreshInterval = value }
   //                 )
   //                 Text(label)
   //             }
   //         }
  //      }
  //  }
//}
  @Composable
  private fun RefreshIntervalSection(settingsDataStore: SettingsDataStore) {
      val refreshInterval by settingsDataStore.refreshIntervalFlow.collectAsState()
      val intervals = listOf(
          stringResourceWithLocale(R.string.auto_refresh_title_hint) to 0,
          stringResourceWithLocale(R.string.fifteen_minutes) to 15,
          stringResourceWithLocale(R.string.sixty_minutes) to 60
      )

      Card(modifier = Modifier.fillMaxWidth()) {
          Column(Modifier.padding(16.dp)) {
              Text(stringResourceWithLocale(R.string.auto_refresh_title), style = MaterialTheme.typography.titleMedium)
              Spacer(modifier = Modifier.height(8.dp))

              intervals.forEach { (label, value) ->
                  Row(
                      modifier = Modifier
                          .fillMaxWidth()
                          .clickable {
                              CoroutineScope(Dispatchers.IO).launch {
                                  settingsDataStore.updateRefreshInterval(value)
                              }
                          }
                          .padding(vertical = 4.dp),
                      verticalAlignment = Alignment.CenterVertically
                  ) {
                      RadioButton(
                          selected = refreshInterval == value,
                          onClick = {
                              CoroutineScope(Dispatchers.IO).launch {
                                  settingsDataStore.updateRefreshInterval(value)
                              }
                          }
                      )
                      Text(label)
                  }
              }
          }
      }
  }


//@Composable
//private fun CurrencySection(settingsRepo: SettingsRepository) {
 //   val currencies = listOf("USD", "EUR", "RUB")
 //   Card(modifier = Modifier.fillMaxWidth()) {
  //      Column(Modifier.padding(16.dp)) {
  //          Text(stringResourceWithLocale(R.string.currency_title), style = MaterialTheme.typography.titleMedium)
  //          Spacer(modifier = Modifier.height(8.dp))
  //          currencies.forEach { currency ->
   //             Row(
   //                 modifier = Modifier
   //                     .fillMaxWidth()
   //                     .clickable { settingsRepo.currency = currency }
   //                     .padding(vertical = 4.dp),
   //                 verticalAlignment = Alignment.CenterVertically
   //             ) {
   //                 RadioButton(
   //                     selected = settingsRepo.currency == currency,
   //                    onClick = { settingsRepo.currency = currency }
    //                )
   //                 Text(currency)
  //              }
    //        }
  //      }
  //  }
//}

@Composable
private fun CurrencySection(settingsDataStore: SettingsDataStore) {
    val currency by settingsDataStore.currencyFlow.collectAsState()
    val currencies = listOf("USD", "EUR", "RUB")

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResourceWithLocale(R.string.currency_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            currencies.forEach { currencyOption ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            CoroutineScope(Dispatchers.IO).launch {
                                settingsDataStore.updateCurrency(currencyOption)
                            }
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currency == currencyOption,
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                settingsDataStore.updateCurrency(currencyOption)
                            }
                        }
                    )
                    Text(currencyOption)
                }
            }
        }
    }
}

//@Composable
//private fun LanguageSection(settingsRepo: SettingsRepository) {
 //   val languages = listOf("🇷🇺 Русский" to "ru", "🇬🇧 English" to "en")

 //   Card(modifier = Modifier.fillMaxWidth()) {
 //       Column(Modifier.padding(16.dp)) {
  //          Text(stringResourceWithLocale(R.string.language_title), style = MaterialTheme.typography.titleMedium)
   //         Spacer(modifier = Modifier.height(8.dp))

   //         languages.forEach { (label, code) ->
   //             Row(
    //                modifier = Modifier
    //                    .fillMaxWidth()
     //                   .clickable { settingsRepo.language = code }  // ✅ Универсально!
    //                    .padding(vertical = 4.dp),
    //                verticalAlignment = Alignment.CenterVertically
    //            ) {
     //               RadioButton(
    //                    selected = settingsRepo.language == code,  // ✅ code вместо "en"!
    //                    onClick = { settingsRepo.language = code }
    //                )
    //                Text(label)
    //            }
    //        }
    //    }
  //  }
//}
   @Composable
   private fun LanguageSection(settingsDataStore: SettingsDataStore) {
       val currentLanguage by settingsDataStore.languageFlow.collectAsState()
       val languages = listOf("🇷🇺 Русский" to "ru", "🇬🇧 English" to "en")

       Card(modifier = Modifier.fillMaxWidth()) {
           Column(Modifier.padding(16.dp)) {
               Text(stringResourceWithLocale(R.string.language_title), style = MaterialTheme.typography.titleMedium)
               Spacer(modifier = Modifier.height(8.dp))

               languages.forEach { (label, code) ->
                   Row(
                       modifier = Modifier
                           .fillMaxWidth()
                           .clickable {
                               CoroutineScope(Dispatchers.IO).launch {
                                   settingsDataStore.updateLanguage(code)
                               }
                           }
                           .padding(vertical = 4.dp),
                       verticalAlignment = Alignment.CenterVertically
                   ) {
                       RadioButton(
                           selected = currentLanguage == code,  // ✅ code вместо "en"!
                           onClick = {
                               CoroutineScope(Dispatchers.IO).launch {
                                   settingsDataStore.updateLanguage(code)
                               }
                           }
                       )
                       Text(label)
                   }
               }
           }
       }
   }



