package com.ahmu.photoweather.weatherapi

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherClient {

    const val KEY = "0673564e144c39a9a5255747b5188355"
    private const val BASE_URL = "http://api.openweathermap.org/data/2.5/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(createHttpLogging())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val weatherApi = retrofit.create(WeatherApi::class.java)

    private fun createHttpLogging(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder().addInterceptor(interceptor).build()
    }

}