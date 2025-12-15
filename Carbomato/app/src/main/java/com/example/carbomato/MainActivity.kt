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

    private lateinit var getContent: ActivityResultLauncher<Intent>
    private lateinit var captureImage: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

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
        setupPermissionLauncher()

        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getContent.launch(intent)
            resetUI()
        }

        captureImageButton.setOnClickListener {
            checkCameraPermissionAndOpen()
        }
    }

    private fun setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            captureImage.launch(intent)
            resetUI()
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show()
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
                            progressBar.visibility = View.GONE
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
                    lifecycleScope.launch {
                        analyze(imageBitmap)
                    }
                }
            } else {
                progressBar.visibility = View.GONE
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
                    text("""
                        Analyze this image and determine if it's a plant. 
                        
                        If it IS a plant:
                        - Identify the plant name
                        - Assess its health condition
                        - List any visible diseases or issues
                        - Provide treatment recommendations
                        - Suggest preventive care tips
                        
                        If it's NOT a plant:
                        - Simply state that this is not a plant image
                        
                        Format the response clearly with proper sections.
                    """.trimIndent())
                    image(bitmap)
                })
            }

            val analysisResult = response.text ?: "No response received"
            resultText.text = Html.fromHtml(
                formatAnalysisText(analysisResult),
                Html.FROM_HTML_MODE_COMPACT
            )

        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("API") == true ->
                    "API Error: Please check your API key in local.properties"
                e.message?.contains("network") == true ->
                    "Network Error: Please check your internet connection"
                else ->
                    "Analysis Failed: ${e.localizedMessage}"
            }
            resultText.text = errorMessage
            e.printStackTrace()
        } finally {
            progressBar.visibility = View.GONE
        }
    }

    private fun formatAnalysisText(text: String): String {
        return text
            .replace("**", "<b>")
            .replace("*", "</b>")
            .replace("\n", "<br>")
    }
}