package com.example.marketwatch.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "instruments")
data class InstrumentEntity(
    @PrimaryKey val symbol: String,
    val price: Double,
    val change: Double?,
    val lastUpdateTime: Long
)