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
                val generativeModel = GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = BuildConfig.GEMINI_API_KEY
                )
                val response = generativeModel.generateContent(content {
                    text("Analyze this plant's health and provide precautions if needed.")
                    image(bitmap)
                })
                _plantAnalysis.postValue(response.text)
            } catch (e: Exception) {
                _plantAnalysis.postValue("Error: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}