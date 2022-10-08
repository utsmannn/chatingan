package com.utsman.chatingan.chat.ui.camera

import androidx.lifecycle.viewModelScope
import com.utsman.chatingan.chat.repository.CameraRepository
import com.utsman.chatingan.chat.routes.BackPassChat
import com.utsman.chatingan.chat.routes.ChatRoute
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.navigation.RouteViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

class CameraViewModel(
    private val cameraRepository: CameraRepository
) : RouteViewModel(ChatRoute.Camera) {
    val imageFileState = cameraRepository.imageFileState

    fun sendFile(file: File) = viewModelScope.launch {
        cameraRepository.useFile(file)
    }

    companion object : KoinInjector {
        override fun inject(): Module {
            return module {
                viewModel { CameraViewModel(get()) }
            }
        }
    }
}