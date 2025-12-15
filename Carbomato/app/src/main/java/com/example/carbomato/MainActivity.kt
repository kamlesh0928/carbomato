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
import android.widget.ImageButton
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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val api_key = BuildConfig.GEMINI_API_KEY

    // UI Components
    private lateinit var selectImageButton: Button
    private lateinit var captureImageButton: Button
    private lateinit var logoutButton: ImageButton
    private lateinit var imageView: ImageView
    private lateinit var resultText: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var getContent: ActivityResultLauncher<Intent>
    private lateinit var captureImage: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selectImageButton = findViewById(R.id.select_image_button)
        captureImageButton = findViewById(R.id.capture_image_button)
        logoutButton = findViewById(R.id.btn_logout) // Initialize Logout
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

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
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
                    // Clear tint if you had one
                    imageView.clearColorFilter()

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
                    imageView.clearColorFilter() // Clear tint
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
        resultText.text = "Scanning plant..."
        progressBar.visibility = View.VISIBLE

        try {
            // Updated to correct model name
            val generativeModel = GenerativeModel(
                modelName = "gemini-2.5-flash",
                apiKey = api_key
            )

            val response = withContext(Dispatchers.IO) {
                generativeModel.generateContent(content {
                    text("""
                        You are an expert botanist. Analyze this image.
                        
                        Output format MUST be strictly as follows (do not use Markdown, use simple text):
                        
                        PLANT NAME: [Name]
                        CONDITION: [Healthy / Unhealthy]
                        DIAGNOSIS: [Briefly describe the disease or issue, if any]
                        
                        REMEDIES:
                        1. [Remedy 1]
                        2. [Remedy 2]
                        3. [Remedy 3]
                        
                        PREVENTION:
                        - [Short prevention tip]
                        
                        Keep the total response concise (under 100 words).
                        If the image is NOT a plant, strictly say: "This does not appear to be a plant."
                    """.trimIndent())
                    image(bitmap)
                })
            }

            val analysisResult = response.text ?: "No response received"

            // Format the text nicely for the TextView
            resultText.text = formatAnalysisText(analysisResult)

        } catch (e: Exception) {
            resultText.text = "Error: ${e.message}"
            e.printStackTrace()
        } finally {
            progressBar.visibility = View.GONE
        }
    }

    private fun formatAnalysisText(text: String): CharSequence {
        // Simple formatting to make headers bold
        val formatted = text
            .replace("PLANT NAME:", "<b>PLANT NAME:</b>")
            .replace("CONDITION:", "<br><b>CONDITION:</b>")
            .replace("DIAGNOSIS:", "<br><b>DIAGNOSIS:</b>")
            .replace("REMEDIES:", "<br><br><b>REMEDIES:</b>")
            .replace("PREVENTION:", "<br><br><b>PREVENTION:</b>")
            .replace("\n", "<br>")

        return Html.fromHtml(formatted, Html.FROM_HTML_MODE_COMPACT)
    }
}