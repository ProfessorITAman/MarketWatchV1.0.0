package com.example.marketwatch.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(

    @PrimaryKey val symbol: String,

    val name: String? = null,

    val price: Double,

    val changePercent: Double,

    val timestamp: Long = System.currentTimeMillis(),

    val lastUpdated: Long = 0L
)
