package com.example.marketwatch.presentaion.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketwatch.data.local.FavoriteDao
import com.example.marketwatch.data.local.FavoriteEntity
import com.example.marketwatch.data.repository.NetworkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoritesViewModel(
    private val dao: FavoriteDao,
    private val networkRepo: NetworkRepository
) : ViewModel() {

    val favorites = dao.getAllFavorites().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun deleteFavorite(symbol: String) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            dao.delete(symbol)
        }
    }

    @Suppress("unused")
    fun addFavorite(favorite: FavoriteEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.insert(favorite)
        }
    }

    @Suppress("unused")
    fun refreshAllPrices() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            try {
                val favorites = dao.getFavoritesSnapshot()
                Log.d("FavoritesVM", "Обновляю ${favorites.size} акций")

                for (favorite in favorites) {
                    val quote = networkRepo.getQuote(favorite.symbol)
                    if (quote != null) {  // ✅ Domain Quote уже Double!
                        dao.updatePrice(
                            symbol = favorite.symbol,
                            price = quote.price  // ✅ String для Room!
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("FavoritesVM", "Refresh failed", e)
            }
        }
    }
}



