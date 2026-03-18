package com.example.marketwatch.data.worker

import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.content.Context
import android.util.Log
import com.example.marketwatch.BuildConfig
import com.example.marketwatch.data.local.AppDatabase
import com.example.marketwatch.data.local.FavoriteDao
import com.example.marketwatch.data.local.SettingsDataStore
import com.example.marketwatch.data.remote.AlphaVantageApi
import com.example.marketwatch.data.repository.NetworkRepository
import com.example.marketwatch.data.repository.NetworkRepositoryImpl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.roundToInt



class QuoteRefreshWorker(
    context: Context,
    params: WorkerParameters,
    //private val favoriteDao: FavoriteDao,
    //private val networkRepo: NetworkRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Room Database
            val favoriteDao = AppDatabase.getDatabase(applicationContext).favoriteDao()

            // Retrofit + API (5 строк!)
            val retrofit = Retrofit.Builder()
                .baseUrl("https://www.alphavantage.co/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val original = chain.request()
                        val url = original.url.newBuilder()
                            .addQueryParameter("apikey", BuildConfig.ALPHA_VANTAGE_API_KEY)
                            .build()
                        val request = original.newBuilder()
                            .url(url)
                            .build()
                        chain.proceed(request)
                    }
                    .build()
                )
                .build()

            val alphaVantageApi = retrofit.create(AlphaVantageApi::class.java)
            val networkRepo = NetworkRepositoryImpl(alphaVantageApi, SettingsDataStore(applicationContext))

            val favorites = favoriteDao.getAllFavoritesSync()
            if (favorites.isEmpty()) {
                Log.i("QuoteWorker", "No favorites to update")
                return Result.success()
            }

            var successCount = 0
            favorites.forEach { favorite ->
                try {
                    val quote = networkRepo.getQuote(favorite.symbol)
                    if (quote != null) {
                        favoriteDao.updateQuote(
                            symbol = favorite.symbol,
                            price = quote.price,  // ✅ Domain Quote уже Double
                            changePercent = quote.changePercent,  // ✅ Уже Double
                            lastUpdated = System.currentTimeMillis()
                        )
                        successCount++
                    }
                } catch (e: Exception) {
                    Log.w("QuoteWorker", "Failed ${favorite.symbol}", e)
                }
            }

            Log.i("QuoteWorker", "Updated $successCount/${favorites.size} favorites")
            Result.success()
        } catch (e: Exception) {
            Log.e("QuoteWorker", "Worker crashed", e)
            Result.retry()
        }
    }
}
