package com.example.cameratestingapp

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cameratestingapp.databinding.ActivityMainBinding
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var imageCapture: ImageCapture? = null

    private lateinit var  outputDirectory: File

    val apiUrl = "placeholder" //api endpoint

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        outputDirectory = getOutputDirectory()
        if (allPermissionGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, Constants.REQUIRED_PERMISSIONS,
                Constants.REQUEST_CODE_PERMISSIONS
            )
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }


        binding.btnTakePhoto.setOnClickListener {
            takePhoto()

        }
    }

    private fun getOutputDirectory(): File{
        val mediaDir =  externalMediaDirs.firstOrNull()?.let { mFile ->
            File(mFile, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }

        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(
            outputDirectory, "productCode.jpg"
        )


        val outputOption = ImageCapture
            .OutputFileOptions
            .Builder(photoFile)
            .build()


        imageCapture.takePicture(
            outputOption, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val code = turnPhotoToCode()
                    requestAPI(code)
                    }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(Constants.TAG, "onError: ${exception.message}", exception)
                    Toast.makeText(
                        this@MainActivity,
                        "Photo Failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }
    private fun turnPhotoToCode(): String {
        if (! Python.isStarted()) {
            Python.start( AndroidPlatform(this));}

        val python = Python.getInstance()
        val pythonFile = python.getModule("photoToCode")
        val photoFile = File(outputDirectory, "productCode.jpg")
        val code = pythonFile.callAttr("BarcodeReader", photoFile.toString()).toString()
        Toast.makeText(this, "Turning photo to code", Toast.LENGTH_SHORT).show()
        Toast.makeText(this, code, Toast.LENGTH_LONG).show()
        return code


    }
    private fun requestAPI(code:String) {
        try {
            val url : URL = URI.create(apiUrl).toURL()
            val connection :HttpURLConnection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"

            val responseCode: Int = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader: BufferedReader =
                    BufferedReader(InputStreamReader(connection.inputStream))
                var line: String?
                val response = StringBuilder()

                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                reader.close()
            }else {
                Toast.makeText(
                    this@MainActivity,
                    "Error, Unable to fetch API data",
                    Toast.LENGTH_SHORT
                ).show()
            }
            connection.disconnect()
        } catch (e: Exception){
            e.printStackTrace()
        }
    }
    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider
            .getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also { mPreview ->

                    mPreview.setSurfaceProvider(
                        binding.viewFinder.surfaceProvider
                    )
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector,
                    preview, imageCapture
                )

            } catch (e: Exception) {
                Log.d(Constants.TAG, "startCamera Fail:", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
        deviceId: Int
    ) {

        if (requestCode == Constants.REQUEST_CODE_PERMISSIONS) {
            startCamera()
        } else {
            Toast.makeText(
                this,
                "Permissions not granted by the user. ",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }


    private fun allPermissionGranted() =
        Constants.REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                baseContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }
}


