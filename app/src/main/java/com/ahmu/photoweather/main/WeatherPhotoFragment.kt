package com.ahmu.photoweather.main

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.ahmu.photoweather.R
import com.ahmu.photoweather.databinding.FragmentWeatherPhotoBinding
import com.ahmu.photoweather.util.LocationUtil
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.CancellationTokenSource
import java.io.File
import kotlin.math.roundToInt

class WeatherPhotoFragment : Fragment() {

    private lateinit var binding: FragmentWeatherPhotoBinding
    private var currentPhotoPath = ""
    private val cancellationTokenSource = CancellationTokenSource()
    private lateinit var viewModel: MainViewModel
    private var showOptionsMenu = false
    private var isPhotoSaved = false
    private var file: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentPhotoPath = it.getString(CURRENT_PHOTO, "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_weather_photo, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Enable back button
        (activity as MainActivity).showBackButton()

        showOptionsMenu = false
        setHasOptionsMenu(true)

        val imageBitmap = BitmapFactory.decodeFile(currentPhotoPath)
        binding.imageView.setImageBitmap(imageBitmap)

        viewModel = ViewModelProvider(this, MainViewModel.MainViewModelFactory(activity!!.application)).get(MainViewModel::class.java)

        val locationUtil = LocationUtil(activity!!, cancellationTokenSource)
        locationUtil.getCurrentLocation().observe(viewLifecycleOwner) { location ->
            if (location != null) {
                viewModel.getCurrentLocation(location)
            }
        }

        listenToObservers()
    }

    private fun listenToObservers() {
        viewModel.weatherModel.observe(viewLifecycleOwner) { weatherModel ->
            if (weatherModel != null) {
                showWeatherViews()
                if (!weatherModel.weather.isNullOrEmpty()) {
                    val iconUrl = "http://openweathermap.org/img/wn/${weatherModel.weather[0].icon}@2x.png"
                    Glide.with(this)
                        .load(iconUrl)
                        .into(binding.icon)

                    binding.description.text = weatherModel.weather[0].description
                }

                binding.placeName.text = weatherModel.name

                // convert kelvin to celsius
                val temp = (weatherModel.main.temp - 273.15).roundToInt()
                binding.temperature.text = "$temp\u2103"

                binding.condition.text = "${weatherModel.wind.speed}km/h"

                showOptionsMenu = true
                activity!!.invalidateOptionsMenu()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) {
            binding.progress.visibility = View.GONE
            Toast.makeText(activity!!, R.string.general_error_label, Toast.LENGTH_SHORT).show()
            Log.e(TAG, "error", it)
        }

        viewModel.share.observe(viewLifecycleOwner) {
            file = it
            isPhotoSaved = true
            binding.progress.visibility = View.GONE
            sharePhoto()
        }

        viewModel.save.observe(viewLifecycleOwner) {
            file = it
            isPhotoSaved = true
            binding.progress.visibility = View.GONE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
            }

            R.id.share -> {
                if (!isPhotoSaved) {
                    binding.progress.visibility = View.VISIBLE
                    viewModel.share(currentPhotoPath, convertImageViewToBitmap())
                } else {
                    sharePhoto()
                }
            }

            R.id.save -> {
                if (!isPhotoSaved) {
                    binding.progress.visibility = View.VISIBLE
                    viewModel.save(currentPhotoPath, convertImageViewToBitmap())
                } else {
                    Toast.makeText(activity!!, "Photo already saved!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return true
    }

    private fun sharePhoto() {
        val uri = getUriForFile()
        if (uri == null) {
            Toast.makeText(activity!!, R.string.general_error_label, Toast.LENGTH_SHORT).show()
            return
        } else {
            ShareCompat.IntentBuilder.from(activity!!)
                .setType("image/jpeg")
                .setStream(uri)
                .startChooser()
        }
    }

    private fun getUriForFile(): Uri? {
        if (file == null) {
            return null
        }

        return FileProvider.getUriForFile(activity!!, "com.ahmu.photoweather.provider", file!!)
    }

    private fun convertImageViewToBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(binding.viewContainer.width, binding.viewContainer.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        binding.viewContainer.draw(canvas)
        return bitmap
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (showOptionsMenu) {
            inflater.inflate(R.menu.share_menu, menu)
            super.onCreateOptionsMenu(menu, inflater)
        }
    }

    private fun showWeatherViews() {
        binding.progress.visibility = View.GONE
        binding.icon.visibility = View.VISIBLE
        binding.description.visibility = View.VISIBLE
        binding.temperature.visibility = View.VISIBLE
        binding.condition.visibility = View.VISIBLE
        binding.placeName.visibility = View.VISIBLE
    }

    override fun onStop() {
        super.onStop()
        cancellationTokenSource.cancel()
    }

    companion object {

        const val CURRENT_PHOTO = "current_photo"
        const val TAG = "WeatherPhotoFragment"

        fun newInstance(photoPath: String) =
            WeatherPhotoFragment().apply {
                arguments = Bundle().apply {
                    putString(CURRENT_PHOTO, photoPath)
                }
            }
    }
}