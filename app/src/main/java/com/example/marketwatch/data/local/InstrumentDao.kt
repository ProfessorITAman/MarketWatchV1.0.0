package com.example.marketwatch.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InstrumentDao{
    @Query("SELECT * FROM instruments WHERE symbol = :symbol LIMIT 1")
    fun observeInstrument(symbol: String): Flow<InstrumentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(instrument: InstrumentEntity)
}