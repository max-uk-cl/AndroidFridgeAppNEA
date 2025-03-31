package com.example.cameratestingapp

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cameratestingapp.databinding.ActivityMainBinding
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alclabs.fasttablelayout.FastTableLayout
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var imageCapture: ImageCapture? = null

    private lateinit var  outputDirectory: File

    private lateinit var openCameraBtn: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        outputDirectory = getOutputDirectory()
        if (allPermissionGranted()) {
            // pass
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
        binding.GetFridge.setOnClickListener {
            getFridgeContents()
            Toast.makeText(this@MainActivity, "Fridge Contents Refreshed",  Toast.LENGTH_SHORT).show()
        }

        binding.btnTakePhoto.setOnClickListener {
            takePhoto()

        }
        openCameraBtn = findViewById(R.id.OpenCameraBtn)
        openCameraBtn.setOnClickListener {
            startCamera()
            openCameraBtn.visibility = View.GONE
            binding.GetFridge.visibility = View.GONE
            binding.FridgeContentsTable.visibility = View.GONE
            binding.viewFinder.visibility = View.VISIBLE
            binding.btnTakePhoto.visibility = View.VISIBLE
            binding.btnGoBack.visibility = View.VISIBLE

        }
        binding.btnGoBack.setOnClickListener {
        binding.viewFinder.visibility = View.GONE
            binding.btnTakePhoto.visibility = View.GONE
            binding.btnGoBack.visibility = View.GONE
            binding.GetFridge.visibility = View.VISIBLE
            binding.FridgeContentsTable.visibility = View.VISIBLE
            openCameraBtn.visibility = View.VISIBLE

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
    private fun getFridgeContents() {
        val context = this
        val directory = context.filesDir
        val file = File(directory, "products.csv")
        val filePath = file.absolutePath

        if (! Python.isStarted()) {
            Python.start( AndroidPlatform(this));}
        val python = Python.getInstance()
        val pythonFileFridgeContents = python.getModule("fridgeContentsGet")
        val pythonFileProductData = python.getModule("returnProduct")
        val productAmount = listOf(pythonFileFridgeContents.callAttr("FridgeContents",filePath)).first().toInt()
        var data = arrayOf<Array<String>>()
        for (i in 0..(productAmount-1)) {
            var productInfo =
                listOf(pythonFileProductData.callAttr("returnProductLine", filePath, i)).first()
                    .toString()
            //println(productInfo)
            val prodCode = productInfo.take(productInfo.indexOf(","))
            var newproductInfo = productInfo.drop(productInfo.indexOf(",") + 1)
            val productName = newproductInfo.take(newproductInfo.indexOf(","))
            val productStock = newproductInfo.drop(newproductInfo.indexOf(",") + 1)
            data += arrayOf(arrayOf(prodCode, productName, productStock))
            if (i == productAmount - 1) {
                binding.FridgeContentsTable.removeAllViews()
                val headers = arrayOf("Code", "Name", "Stock")
                val fastTable = FastTableLayout(this, binding.FridgeContentsTable, headers, data)
                fastTable.SET_DEAFULT_HEADER_BORDER = true
                fastTable.SET_DEFAULT_HEADER_BACKGROUND = true
                fastTable.setCustomBackgroundToHeaders(R.color.white)
                fastTable.build()

                Toast.makeText(this@MainActivity, "Fridge Contents Refreshed", Toast.LENGTH_SHORT)
                    .show()

            }
        }
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
                    turnPhotoToCode()
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
    private fun turnPhotoToCode() {
        if (! Python.isStarted()) {
            Python.start( AndroidPlatform(this));}

        val python = Python.getInstance()
        val pythonFileCode = python.getModule("photoToCode")
        val pythonFileProductInfo = python.getModule("apicall")
        val photoFile = File(outputDirectory, "productCode.jpg")
        val pythonFileUpdateStock = python.getModule("updateStock")
        val code = pythonFileCode.callAttr("BarcodeReader", photoFile.toString()).toString()
        val builder = AlertDialog.Builder(this)
        if (code != "error") {
            val productName = pythonFileProductInfo.callAttr("ProductInfo", code).toString()
            if (productName != "") {
                builder.setTitle("Product Info")
                builder.setMessage("Product: $productName")
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                builder.setCancelable(false)
                builder.setPositiveButton("Add to Fridge") { dialog, which ->
                    pythonFileUpdateStock.callAttr("addItem", code)
                }
                builder.setNegativeButton("Remove from Fridge") { dialog, which ->
                    pythonFileUpdateStock.callAttr("removeItem", code)
                }
                builder.setNeutralButton("Cancel") { dialog, which ->
                    dialog.cancel()
                }
                val dialog = builder.create()
                dialog.show()
            }
            else {
                DisplayErrorProdNotFound(code)
            }
        }
        else {
            DisplayErrorCodeNotFound()
        }
    }
    private fun DisplayErrorCodeNotFound() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Code not found")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setCancelable(false)
        builder.setPositiveButton("OK") { dialog, which ->
            dialog.cancel()
        }
        val dialog = builder.create()
        dialog.show()
    }
    private fun DisplayErrorProdNotFound(code: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Product code: $code not recognized")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setCancelable(false)
        builder.setPositiveButton("OK") { dialog, which ->
            dialog.cancel()
        }
        val dialog = builder.create()
        dialog.show()
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


