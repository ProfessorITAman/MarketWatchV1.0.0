package com.example.marketwatch.di

import com.example.marketwatch.BuildConfig
import com.example.marketwatch.data.remote.AlphaVantageApi
import com.example.marketwatch.data.remote.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {
    single {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor { chain ->
                val original = chain.request()
                val url = original.url.newBuilder()
                    .addQueryParameter("apikey", BuildConfig.ALPHA_VANTAGE_API_KEY)
                    .build()
                chain.proceed(original.newBuilder().url(url).build())
            }
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl("https://www.alphavantage.co/")
            .client(get<OkHttpClient>())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<ApiService> { get<Retrofit>().create(ApiService::class.java) }
    single<AlphaVantageApi> { get<Retrofit>().create(AlphaVantageApi::class.java) }
}

