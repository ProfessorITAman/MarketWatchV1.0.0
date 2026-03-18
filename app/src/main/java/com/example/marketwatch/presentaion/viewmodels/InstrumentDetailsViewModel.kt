package com.example.marketwatch.presentaion.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketwatch.data.repository.NetworkRepository
import com.example.marketwatch.data.repository.SettingsRepository
import com.example.marketwatch.presentaion.MVI.InstrumentDetailsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InstrumentDetailsViewModel(
    private val networkRepository: NetworkRepository,
   // private val settingsRepo: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InstrumentDetailsUiState())
    val uiState: StateFlow<InstrumentDetailsUiState> = _uiState.asStateFlow()

    private var currentSymbol: String = ""

    fun loadInstrument(symbol: String) {
        currentSymbol = symbol
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val quote = networkRepository.getQuote(symbol)
                _uiState.update {
                    it.copy(
                        symbol = symbol,
                        price = quote?.price?.toString() ?: "N/A",     // ✅ toString()
                        change = quote?.changePercent?.toString() ?: "—", // ✅ toString()
                        lastUpdateTime = quote?.latestTradingDay ?: "N/A",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        price = "❌ Ошибка",
                        change = "—",
                        lastUpdateTime = "",
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }


    fun refreshInstrument() {
        loadInstrument(currentSymbol)
    }
}