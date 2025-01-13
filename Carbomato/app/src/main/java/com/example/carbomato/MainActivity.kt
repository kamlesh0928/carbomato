package com.example.carbomato

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    private val API_KEY = "AIzaSyDLMtZJeW_Hg6zWAdi-_baHXCeDguHJ0Jo"

    private lateinit var getContent: ActivityResultLauncher<Intent>
    private lateinit var captureImage: ActivityResultLauncher<Intent>
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private lateinit var select_image_button: Button
    private lateinit var capture_image_button: Button

    private lateinit var image_view: ImageView
    private lateinit var result_text: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        select_image_button = findViewById(R.id.select_image_button)
        image_view = findViewById(R.id.image_view)
        result_text = findViewById(R.id.result_text)
        progressBar = findViewById(R.id.progressBar)
        capture_image_button = findViewById(R.id.capture_image_button)

        getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri = result.data?.data
                if (imageUri != null) {
                    Glide.with(this).load(imageUri).into(image_view)

                    coroutineScope.launch {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                        val outputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        withContext(Dispatchers.IO) {
                            analyze(bitmap)
                        }

                    }
                }
            }
        }

        captureImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                image_view.setImageBitmap(imageBitmap)

                coroutineScope.launch {
                    val outputStream = ByteArrayOutputStream()
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

                    withContext(Dispatchers.IO) {
                        analyze(imageBitmap)
                    }
                }
            }
        }

        select_image_button.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getContent.launch(intent)
            result_text.text=""
            progressBar.visibility = android.view.View.VISIBLE

        }

        capture_image_button.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            captureImage.launch(intent)
            result_text.text=""
            progressBar.visibility = android.view.View.VISIBLE

        }
    }

    private suspend fun analyze(bitmap: Bitmap){
        val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = API_KEY
        )
        val response = generativeModel.generateContent(content {
            text("Plant health")
            image(bitmap)
        })
        withContext(Dispatchers.Main){
            result_text.text = Html.fromHtml(response.text, Html.FROM_HTML_MODE_COMPACT)
            progressBar.visibility = android.view.View.GONE

        }
    }
}
