package com.ahmu.photoweather.weatherapi

data class WeatherModel(
    val coord: Coord, val weather: List<Weather>, val base: String, val main: Main, val visibility: Int,
    val wind: Wind, val clouds: Clouds, val dt: Int, val sys: Sys, val timezone: Int, val id: Int,
    val name: String, val code: Int
)
data class Coord(val lon: Double, val lat: Double)
data class Weather(val id: Int, val main: String, val description: String, val icon: String)
data class Main(val temp: Double, val feels_like: Double, val temp_min: Double, val temp_max: Double, val pressure: Int, val humidity: Int)
data class Wind(val speed: Double, val deg: Int)
data class Clouds(val all: Int)
data class Sys(val type: Int, val id: Int, val country: String, val sunrise: String, val sunset: String)

////////////////////////////////////////////////////
//data class WeatherModel (
//    val coord: Coord, val weather: List<Weather>, val base: String,
//    val main: Main, val visibility: Long, val wind: Wind, val clouds: Clouds, val dt: Long,
//    val sys: Sys, val timezone: Long, val id: Long, val name: String, val cod: Long
//)
//
//data class Clouds (
//    val all: Long
//)
//
//data class Coord (
//    val lon: Double,
//    val lat: Double
//)
//
//data class Main (
//    val temp: Double,
//
//    @Json(name = "feels_like")
//    val feelsLike: Double,
//
//    @Json(name = "temp_min")
//    val tempMin: Double,
//
//    @Json(name = "temp_max")
//    val tempMax: Double,
//
//    val pressure: Long,
//    val humidity: Long
//)
//
//data class Sys (
//    val type: Long,
//    val id: Long,
//    val country: String,
//    val sunrise: Long,
//    val sunset: Long
//)
//
//data class Weather (
//    val id: Long,
//    val main: String,
//    val description: String,
//    val icon: String
//)
//
//data class Wind (
//    val speed: Double,
//    val deg: Long
//)
