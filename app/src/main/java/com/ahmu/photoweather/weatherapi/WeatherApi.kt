package com.ahmu.photoweather.weatherapi

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("weather")
    suspend fun getWeatherByLatAndLong(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") id: String = WeatherClient.KEY
    ): WeatherModel
}