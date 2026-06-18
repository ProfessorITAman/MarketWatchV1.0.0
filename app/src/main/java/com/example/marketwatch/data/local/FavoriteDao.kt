package com.example.marketwatch.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE symbol = :symbol")
    suspend fun delete(symbol: String) 

    @Update
    suspend fun updateFavorite(favorite: FavoriteEntity) 

    @Query("SELECT * FROM favorites ORDER BY lastUpdated DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>> 

    @Query("SELECT * FROM favorites")
    suspend fun getFavoritesSnapshot(): List<FavoriteEntity>

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
