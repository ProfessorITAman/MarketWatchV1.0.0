package com.example.marketwatch.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) для работы с таблицей избранных акций.
 *
 * Room генерирует реализацию интерфейса на этапе компиляции.
 * Типобезопасные SQL запросы с автогенерацией кода.
 *
 * ⚠️ ВНИМАНИЕ: insert() и delete() НЕ suspend - выполняются в IO потоке Repository!
 * getAllFavorites() возвращает Flow для реактивного UI (автообновление списка).
 */
@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)  // ✅ suspend!

    @Query("DELETE FROM favorites WHERE symbol = :symbol")
    suspend fun delete(symbol: String)  // ✅ suspend!

    @Update  // ✅ @Update для update!
    suspend fun updateFavorite(favorite: FavoriteEntity)  // ✅ suspend!

    @Query("SELECT * FROM favorites ORDER BY lastUpdated DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>  // Flow = НЕ suspend!

    @Query("SELECT * FROM favorites")
    suspend fun getFavoritesSnapshot(): List<FavoriteEntity>

    // ✅ НОВОЕ для WorkManager
    @Query("SELECT * FROM favorites")
    suspend fun getAllFavoritesSync(): List<FavoriteEntity>

    @Query("""
        UPDATE favorites 
        SET price = :price, changePercent = :changePercent, lastUpdated = :lastUpdated
        WHERE symbol = :symbol
    """)
    suspend fun updateQuote(
        symbol: String,
        price: Double,
        changePercent: Double,
        lastUpdated: Long
    )

    @Query("UPDATE favorites SET price = :price, lastUpdated = :lastUpdated WHERE symbol = :symbol")
    suspend fun updatePrice(symbol: String, price: Double, lastUpdated: Long = System.currentTimeMillis())
}
