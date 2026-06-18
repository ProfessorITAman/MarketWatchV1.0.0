package com.example.marketwatch.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TimeSeriesResponse(
    @SerializedName("Time Series (Daily)")
    val timeSeriesDaily: Map<String, TimeSeriesData>
)
