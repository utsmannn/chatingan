package com.utsman.chatingan.chat.ui.camera

import androidx.lifecycle.viewModelScope
import com.utsman.chatingan.chat.repository.CameraRepository
import com.utsman.chatingan.chat.repository.ChatRepository
import com.utsman.chatingan.chat.routes.ChatRoute
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.model.Message
import com.utsman.chatingan.navigation.RouteViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

class CameraViewModel(
    private val cameraRepository: CameraRepository,
    private val chatRepository: ChatRepository
) : RouteViewModel(ChatRoute.Camera) {
    val imageFileState = cameraRepository.imageFileState

    fun setFile(file: File) = viewModelScope.launch {
        cameraRepository.useFile(file)
    }

    fun clearFile() = viewModelScope.launch {
        cameraRepository.clearFile()
    }

    fun sendMessage(contact: Contact, message: Message) =
        chatRepository.sendMessage(viewModelScope, contact, message)

    companion object : KoinInjector {
        override fun inject(): Module {
            return module {
                viewModel { CameraViewModel(get(), get()) }
            }
        }
    }
}