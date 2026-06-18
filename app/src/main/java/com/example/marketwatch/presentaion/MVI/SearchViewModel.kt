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

class SearchViewModel(
    private val apiService: ApiService,
    private val favoriteDao: FavoriteDao,
  //  private val settingsRepo: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchState())
    val uiState: StateFlow<SearchState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String?>(extraBufferCapacity = 1)
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    fun clearSnackbar() {
        _snackbarMessage.tryEmit(null)
    }

    fun onSearch(ticker: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true)
        try {
            val response = apiService.getQuote(symbol = ticker, apikey = "CZJ3ZYDNQBDXQ8S6")

            Log.d("SearchViewModel", "РЕАЛЬНЫЙ JSON: ${Gson().toJson(response)}")
            Log.d("SearchViewModel", " QUOTE: ${response.globalQuote}")

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

    fun addToFavorites(quote: GlobalQuote) = viewModelScope.launch {
        try {
            val favorite = FavoriteEntity(
                symbol = quote.symbol,
                price = quote.price.toDoubleOrNull() ?: 0.0, 
                changePercent = quote.changePercent
                    .replace("%", "") 
                    .toDoubleOrNull() ?: 0.0  
            )
            withContext(Dispatchers.IO) { 
                favoriteDao.insert(favorite)  
            }
            showError(" ${quote.symbol} добавлено!")
        } catch (e: Exception) {
            Log.e("SearchViewModel", "API error", e)  
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Неизвестная ошибка"
            )
            showError(" Ошибка добавления")
        }
    }

    private fun showError(message: String) {
        _snackbarMessage.tryEmit(message)
    }
}

