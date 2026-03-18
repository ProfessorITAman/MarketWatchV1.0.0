package com.example.marketwatch.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TimeSeriesData(
    @SerializedName("1. open") val open: String,
    @SerializedName("4. close") val close: String
)