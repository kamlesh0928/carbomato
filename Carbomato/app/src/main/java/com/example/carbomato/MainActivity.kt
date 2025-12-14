package com.example.carbomato

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val API_KEY = BuildConfig.GEMINI_API_KEY
    private val CAMERA_PERMISSION_CODE = 100

    private lateinit var getContent: ActivityResultLauncher<Intent>
    private lateinit var captureImage: ActivityResultLauncher<Intent>

    private lateinit var selectImageButton: Button
    private lateinit var captureImageButton: Button
    private lateinit var imageView: ImageView
    private lateinit var resultText: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selectImageButton = findViewById(R.id.select_image_button)
        captureImageButton = findViewById(R.id.capture_image_button)
        imageView = findViewById(R.id.image_view)
        resultText = findViewById(R.id.result_text)
        progressBar = findViewById(R.id.progressBar)

        setupImagePickers()

        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getContent.launch(intent)
            resetUI()
        }

        captureImageButton.setOnClickListener {
            checkCameraPermissionAndOpen()
        }
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        captureImage.launch(intent)
        resetUI()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupImagePickers() {
        getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri = result.data?.data
                if (imageUri != null) {
                    Glide.with(this).load(imageUri).into(imageView)

                    lifecycleScope.launch {
                        try {
                            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                            analyze(bitmap)
                        } catch (e: Exception) {
                            resultText.text = "Error loading image: ${e.localizedMessage}"
                        }
                    }
                }
            }
        }

        captureImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                if (imageBitmap != null) {
                    imageView.setImageBitmap(imageBitmap)
                    // Launch analysis
                    lifecycleScope.launch {
                        analyze(imageBitmap)
                    }
                }
            }
        }
    }

    private fun resetUI() {
        resultText.text = "Analyzing..."
        progressBar.visibility = View.VISIBLE
    }

    private suspend fun analyze(bitmap: Bitmap) {
        resultText.text = "Analyzing..."
        progressBar.visibility = View.VISIBLE

        try {
            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = API_KEY
            )

            val response = withContext(Dispatchers.IO) {
                generativeModel.generateContent(content {
                    text("Analyze this plant's health...")
                    image(bitmap)
                })
            }

            resultText.text = Html.fromHtml(response.text ?: "No response text", Html.FROM_HTML_MODE_COMPACT)

        } catch (e: Exception) {
            resultText.text = "Analysis Failed. Please check your API Key.\n\nDetails: ${e.localizedMessage}"
            e.printStackTrace()
        } finally {
            progressBar.visibility = View.GONE
        }
    }
}