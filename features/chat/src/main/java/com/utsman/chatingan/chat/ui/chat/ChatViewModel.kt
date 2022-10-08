package com.utsman.chatingan.chat.ui.chat

import androidx.lifecycle.viewModelScope
import com.utsman.chatingan.chat.repository.CameraRepository
import com.utsman.chatingan.chat.repository.ChatRepository
import com.utsman.chatingan.chat.routes.BackPassChat
import com.utsman.chatingan.chat.routes.ChatRoute
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.navigation.RouteViewModel
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.entity.MessageChat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

class ChatViewModel(
    private val repository: ChatRepository,
    private val cameraRepository: CameraRepository
) : RouteViewModel(ChatRoute.Chat) {
    private val rawText = MutableStateFlow("")
    private val isTyping = MutableStateFlow(false)

    val chatState = repository.chatState
    val textState = rawText
    val receiverTyping = repository.isReceiverIsTyping

    val imageFileState = cameraRepository.imageFileState

    fun getChat(contact: Contact) {
        viewModelScope.launch {
            repository.getChat(contact)
        }
        viewModelScope.launch {
            rawText.filter { it.length > 2 }
                .collect {
                    isTyping.value = true

                    delay(1000)
                    isTyping.value = false
                }
        }

        viewModelScope.launch {
            repository.getChatInfo(contact)
        }
    }

    fun readChat(contact: Contact, messageChat: MessageChat) {
        viewModelScope.launch {
            delay(500)
            repository.readChat(contact, messageChat)
        }
    }

    fun sendMessage(contact: Contact, message: String) = viewModelScope.launch {
        //repository.sendMessage(contact, message)
        val imageFileResult = cameraRepository.imageFileState.value
        if (imageFileResult.isFailure) {
            repository.sendMessage(contact, message)
        }
        if (imageFileResult.isSuccess) {
            val imageFile = imageFileResult.getOrThrow()
            repository.sendImage(contact, message, imageFile)
        }
    }

    fun sendImage(contact: Contact, message: String, file: File) = viewModelScope.launch {
        repository.sendImage(contact, message, file)
    }

    fun listenForTyping(contact: Contact) = viewModelScope.launch {
        isTyping.debounce(200)
            .distinctUntilChanged()
            .collect {
                repository.setTypingStatus(contact, it)
            }
    }

    fun setText(text: String) = viewModelScope.launch {
        rawText.value = text
    }

    override fun dispose() {
        viewModelScope.launch {
            repository.dispose()
            cameraRepository.clearFile()
        }
    }

    companion object : KoinInjector {
        override fun inject(): Module {
            return module {
                viewModel { ChatViewModel(get(), get()) }
            }
        }
    }

}