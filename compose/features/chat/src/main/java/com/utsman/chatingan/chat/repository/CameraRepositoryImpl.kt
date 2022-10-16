package com.utsman.chatingan.chat.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class CameraRepositoryImpl : CameraRepository {

    private val _imageFileState = MutableStateFlow(Result.failure<File>(Throwable("Empty")))
    override val imageFileState: StateFlow<Result<File>>
        get() = _imageFileState

    override suspend fun useFile(file: File) {
        _imageFileState.value = Result.success(file)
    }

    override suspend fun clearFile() {
        _imageFileState.value = Result.failure(Throwable("Empty"))
    }
}