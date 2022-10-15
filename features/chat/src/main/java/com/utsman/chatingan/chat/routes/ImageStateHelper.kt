package com.utsman.chatingan.chat.routes

import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

class ImageStateHelper {
    private val _imageFileFlow: MutableStateFlow<ImageState> = MutableStateFlow(ImageState())

    val imageFile: Flow<ImageState>
        get() = _imageFileFlow

    fun setImage(id: String, file: File) {
        _imageFileFlow.value = ImageState(id, file)
    }

    fun clear() {
        _imageFileFlow.value = ImageState()
    }

    data class ImageState(
        val sessionId: String = "",
        val file: File? = null
    )
}

val LocalImageStateHelper = compositionLocalOf { ImageStateHelper() }