package com.example.marketwatch.ui.screens


import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marketwatch.presentaion.MVI.SearchViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.material3.*
import androidx.navigation.NavHostController
import com.example.marketwatch.R
import com.example.marketwatch.ui.theme.stringResourceWithLocale

/**
 * ✅ SearchScreen - главный экран поиска котировок.
 *
 * UI Flow: Тикер → API → GlobalQuote → Кнопки (Детали/Избранное).
 * Реактивное состояние через SearchState + StateFlow.
 */
@Composable
fun SearchScreen(navController: NavHostController) {
    val viewModel: SearchViewModel = koinViewModel() // ✅ Koin DI
    val state by viewModel.uiState.collectAsState() // ✅ SearchState (loading/error/quote)
    var ticker by remember { mutableStateOf("") } // ✅ Локальное поле ввода

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Заголовок
        Text(
            text = stringResourceWithLocale(R.string.search),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Поле поиска с иконкой
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = ticker,
                    onValueChange = { newTicker ->
                        ticker = newTicker.uppercase().replace(Regex("[^A-Z0-9]"), "")
                    },
                    label = { Text(stringResourceWithLocale(R.string.search_ticker_hint)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
        // Кнопка поиска
        Button(
            onClick = {
                Log.d("SearchScreen", "🔍 Поиск: '$ticker'") // ← ДОБАВЬ!
                viewModel.onSearch(ticker)
                Log.d("SearchScreen", "✅ onSearch вызван")  // ← ДОБАВЬ!
            },
            enabled = ticker.length >= 2 && !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = if (state.isLoading) stringResourceWithLocale(R.string.loading) else stringResourceWithLocale(
                        R.string.search
                    ),
                    fontSize = 16.sp
                )
            }
        }

        // Результат в красивой карточке
        state.quote?.let { quote ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "${quote.symbol}: $${quote.price}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "${stringResourceWithLocale(R.string.change)}: ${quote.changePercent}"
                    )
                    Text(
                        text = "${stringResourceWithLocale(R.string.date)}: ${quote.latestTradingDay}"
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // ✅ КНОПКА 1: Подробнее (уже работает)
                    FilledTonalButton(
                        onClick = { navController.navigate(Screen.Detail.createRoute(quote.symbol)) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResourceWithLocale(R.string.view_details))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ✅ КНОПКА 2: Избранное (заглушка для Room)
                    OutlinedButton(
                        onClick = {
                            viewModel.addToFavorites(quote)  // Новый метод в ViewModel!
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResourceWithLocale(R.string.add_to_favorites))
                    }
                }
            }
        }
    }
}







