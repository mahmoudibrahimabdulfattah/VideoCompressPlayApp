package com.example.vedioapp

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VideoViewModel(application: Application) : AndroidViewModel(application) {
    private val videoRepository = VideoRepository(application)
    private val sharedPreferences = application.getSharedPreferences("video_prefs", Context.MODE_PRIVATE)
    private val _videoUris = MutableStateFlow<List<Uri>>(emptyList())
    val videoUris: StateFlow<List<Uri>> = _videoUris

    init {
        loadSavedVideoUris()
    }

    private fun loadSavedVideoUris() {
        val savedUrisString = sharedPreferences.getStringSet("saved_video_uris", emptySet())
        val uris = savedUrisString?.map { Uri.parse(it) } ?: emptyList()
        _videoUris.value = uris
    }

    fun addVideoUri(uri: Uri) {
        viewModelScope.launch {
            val savedUri = videoRepository.compressAndSaveVideo(uri)
            savedUri?.let {
                val updatedUris = _videoUris.value + it
                _videoUris.value = updatedUris
                saveVideoUris(updatedUris)
            }
        }
    }

    private fun saveVideoUris(uris: List<Uri>) {
        val urisString = uris.map { it.toString() }.toSet()
        sharedPreferences.edit().putStringSet("saved_video_uris", urisString).apply()
    }
}
