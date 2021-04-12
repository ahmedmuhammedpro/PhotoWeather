package com.ahmu.photoweather.main

import android.app.Application
import android.graphics.Bitmap
import android.location.Location
import androidx.lifecycle.*
import com.ahmu.photoweather.weatherapi.WeatherClient
import com.ahmu.photoweather.weatherapi.WeatherModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import kotlin.jvm.Throws

class MainViewModel(context: Application) : AndroidViewModel(context) {


    private val mWeatherModel = MutableLiveData<WeatherModel?>()
    val weatherModel: LiveData<WeatherModel?> = mWeatherModel

    private val mError = MutableLiveData<Exception>()
    val error: LiveData<Exception> = mError

    private val mSave = MutableLiveData<File>()
    val save: LiveData<File> = mSave

    private val mShare = MutableLiveData<File>()
    val share: LiveData<File> = mShare

    fun getCurrentLocation(location: Location) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val api = WeatherClient.weatherApi
                val result = api.getWeatherByLatAndLong(location.latitude, location.longitude)
                withContext(Dispatchers.Main) {
                    mWeatherModel.value = result
                }
            } catch (ex: Exception) {
                withContext(Dispatchers.Main) {
                    mError.value = ex
                }
            }
        }
    }

    fun save(currentPath: String, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = saveImage(currentPath, bitmap)
                withContext(Dispatchers.Main) {
                    mSave.value = file
                }
            } catch (ex: Exception) {
                withContext(Dispatchers.Main) {
                    mError.value = ex
                }
            }
        }
    }

    fun share(currentPath: String, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = saveImage(currentPath, bitmap)
                withContext(Dispatchers.Main) {
                    mShare.value = file
                }
            } catch (ex: Exception) {
                withContext(Dispatchers.Main) {
                    mError.value = ex
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun saveImage(currentPath: String, bitmap: Bitmap): File {
        val file = File(currentPath)
        val imageFile = File(file.parentFile, "${file.nameWithoutExtension}.png")
        file.delete()
        val fileOutputStream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()

        return imageFile
    }

    class MainViewModelFactory(private val context: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(context) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class!")
        }

    }
}