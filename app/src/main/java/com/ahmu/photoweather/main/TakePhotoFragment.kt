package com.ahmu.photoweather.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.ahmu.photoweather.R
import com.ahmu.photoweather.databinding.FragmentTakePhotoBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class TakePhotoFragment : Fragment() {

    private lateinit var binding: FragmentTakePhotoBinding
    private lateinit var currentPhotoPath: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_take_photo, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).hideBackButton()

        binding.takePhoto.setOnClickListener {
            checkFileAndLocationPermissions()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAMERA_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            val fragmentManager = activity!!.supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction
                .replace(R.id.container, WeatherPhotoFragment.newInstance(currentPhotoPath))
                .addToBackStack(null)
            fragmentTransaction.commit()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            REQUEST_FILE_PERMISSION -> {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    val rationale = ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permissions[0])
                    if (!rationale) {
                        showPermissionSnackBar()
                        return
                    }

                    showErrorToast(getString(R.string.permissions_error_label))
                    return
                }

                if (checkLocationPermissions()) {
                    if (isGpsEnabled()) {
                        openCamera()
                    } else {
                        showErrorToast(getString(R.string.enable_gps_label))
                    }
                } else {
                    requestLocationPermissions()
                }

            }

            REQUEST_LOCATION_PERMISSION -> {
                for (i in grantResults.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        val rationale = ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permissions[i])
                        if (!rationale) {
                            showPermissionSnackBar()
                            return
                        }

                        showErrorToast(getString(R.string.permissions_error_label))
                        return
                    }
                }

                if (isGpsEnabled()) {
                    openCamera()
                }
            }


        }

    }

    private fun checkFileAndLocationPermissions() {
        if (checkFilePermissions()) {
            if (checkLocationPermissions()) {
                if (isGpsEnabled()) {
                    openCamera()
                } else {
                    showErrorToast(getString(R.string.enable_gps_label))
                }
            } else {
                requestLocationPermissions()
            }
        } else {
            requestFilePermissions()
        }
    }

    private fun checkFilePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestFilePermissions() {
        requestPermissions(
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_FILE_PERMISSION
        )
    }

    private fun requestLocationPermissions() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }

    private fun isGpsEnabled():Boolean {
        val locationManager = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePhotoIntent ->
            val photoFile: File? = try {
                createPhotoFile()
            } catch (ex: IOException) {
                showErrorToast(getString(R.string.general_error_label))
                null
            }

            photoFile?.also {
                val photoUri = FileProvider.getUriForFile(activity!!, "com.ahmu.photoweather.provider", it)
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(takePhotoIntent, REQUEST_CAMERA_CAPTURE)
            }
        }
    }

    @Throws(IOException::class)
    @Suppress("DEPRECATION")
    private fun createPhotoFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storage = Environment.getExternalStorageDirectory()
        val dir = File(storage.absolutePath + "/" + getString(R.string.app_name))
        dir.mkdirs()
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", dir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun showPermissionSnackBar() {
        val permissionsSnackBar = Snackbar.make(binding.root, R.string.enable_permissions_label, Snackbar.LENGTH_LONG)
        permissionsSnackBar.setAction(R.string.enable_permissions_button_label) {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                val uri = Uri.fromParts("package", activity!!.packageName, null)
                data = uri
                startActivity(this)
            }
        }

        permissionsSnackBar.show()
    }

    private fun showErrorToast(msg: String) {
        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
    }

    companion object {
        const val REQUEST_FILE_PERMISSION = 101
        const val REQUEST_LOCATION_PERMISSION = 102
        const val REQUEST_CAMERA_CAPTURE = 201

        fun newInstance() = TakePhotoFragment().apply { }
    }

}