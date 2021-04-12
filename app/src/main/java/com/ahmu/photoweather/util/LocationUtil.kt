package com.ahmu.photoweather.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task

class LocationUtil(private val context: Context, private val cancellationTokenSource: CancellationTokenSource) {

    private val currentLocation = MutableLiveData<Location?>()

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(): LiveData<Location?> {

        val currentLocationTask: Task<Location> = fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        )

        currentLocationTask.addOnCompleteListener { task: Task<Location> ->
            if (task.isSuccessful && task.result != null) {
                currentLocation.value = task.result
            } else {
                Log.e(TAG, "error", task.exception)
            }
        }

        return currentLocation
    }

    companion object {
        const val TAG = "LocationUtil"
    }

}