package com.example.marketwatch.presentaion.MVI

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketwatch.presentaion.MVI.SearchState
import com.example.marketwatch.data.remote.ApiService
import com.example.marketwatch.data.local.FavoriteDao
import com.example.marketwatch.data.local.FavoriteEntity
import com.example.marketwatch.data.remote.dto.GlobalQuote
import com.example.marketwatch.data.repository.SettingsRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/**
 * ViewModel экрана поиска котировок.
 *
 * Управляет состоянием UI: поиск → API → Room (избранное) + Snackbar.
 * MVVM + Unidirectional Data Flow (UDF).
 */
class SearchViewModel(
    private val apiService: ApiService, // ✅ AlphaVantageService (Retrofit)
    private val favoriteDao: FavoriteDao,
  //  private val settingsRepo: SettingsRepository// ✅ Room DAO для избранного
) : ViewModel() {

    /**
     * ✅ Главное состояние UI (SearchState).
     * Reactivno обновляет Compose: loading → quote/error → кнопки.
     */
    private val _uiState = MutableStateFlow(SearchState())
    val uiState: StateFlow<SearchState> = _uiState.asStateFlow()

    /**
     * ✅ Snackbar сообщения (одноразовые уведомления).
     * extraBufferCapacity = 1 = не теряем сообщение при быстрых кликах.
     */
    private val _snackbarMessage = MutableSharedFlow<String?>(extraBufferCapacity = 1)
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    /** Очистить Snackbar (кнопка Dismiss) */
    fun clearSnackbar() {
        _snackbarMessage.tryEmit(null)
    }

    /**
     * ✅ Поиск котировок по тикеру.
     * UI Flow: Loading → API → Success/Error → Snackbar.
     */
    fun onSearch(ticker: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true)
        try {
            val response = apiService.getQuote(symbol = ticker, apikey = "CZJ3ZYDNQBDXQ8S6")

            Log.d("SearchViewModel", "🔍 РЕАЛЬНЫЙ JSON: ${Gson().toJson(response)}")
            Log.d("SearchViewModel", "✅ QUOTE: ${response.globalQuote}")  // ← ЭТО ПОКАЖЕТ!

            _uiState.value = _uiState.value.copy(
                quote = response.globalQuote,
                isLoading = false,
                error = null
            )
        } catch (e: Exception) {
            Log.e("SearchViewModel", "❌ ${e.message}")
            _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
        }
    }

    /**
     * ✅ Добавить котировку в Room БД (избранное).
     * Создает FavoriteEntity → insert() → Snackbar "Добавлено!".
     */
    fun addToFavorites(quote: GlobalQuote) = viewModelScope.launch {
        try {
            val favorite = FavoriteEntity(
                symbol = quote.symbol, // AAPL
                price = quote.price.toDoubleOrNull() ?: 0.0, // 150.25 (String→Double)
                changePercent = quote.changePercent
                    .replace("%", "")  // Убираем %
                    .toDoubleOrNull() ?: 0.0  // String → Double
            )
            withContext(Dispatchers.IO) { // ✅ БД в IO потоке
                favoriteDao.insert(favorite)  // ✅ REPLACE если уже есть
            }
            showError("✅ ${quote.symbol} добавлено!")
        } catch (e: Exception) {
            Log.e("SearchViewModel", "API error", e)  // ← Логируем!
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Неизвестная ошибка"
            )
            showError("❌ Ошибка добавления")
        }
    }

    /** Приватный эмиттер Snackbar */
    private fun showError(message: String) {
        _snackbarMessage.tryEmit(message)
    }
}

