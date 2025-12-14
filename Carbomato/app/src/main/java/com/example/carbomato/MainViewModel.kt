package com.example.carbomato

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _plantAnalysis = MutableLiveData<String>()
    val plantAnalysis: LiveData<String> = _plantAnalysis

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun analyzePlant(bitmap: Bitmap) {
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fixed model name
                val generativeModel = GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = BuildConfig.GEMINI_API_KEY
                )

                val response = generativeModel.generateContent(content {
                    text("Analyze this plant's health. If it is a plant, identify it, diagnose any diseases, and provide precautions/remedies. If it is not a plant, say so.")
                    image(bitmap)
                })

                _plantAnalysis.postValue(response.text!!)
            } catch (e: Exception) {
                _plantAnalysis.postValue("<b>Error:</b> ${e.localizedMessage}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}