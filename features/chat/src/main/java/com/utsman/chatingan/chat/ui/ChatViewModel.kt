package com.utsman.chatingan.chat.ui

import androidx.lifecycle.viewModelScope
import com.utsman.chatingan.chat.repository.ChatRepository
import com.utsman.chatingan.chat.routes.ChatRoute
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.navigation.RouteViewModel
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.entity.MessageChat
import com.utsman.chatingan.sdk.utils.ChatinganUtils
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

class ChatViewModel(
    private val repository: ChatRepository
) : RouteViewModel(ChatRoute.Chat) {
    private val rawText = MutableStateFlow("")
    private val isTyping = MutableStateFlow(false)

    val chatState = repository.chatState
    val textState = rawText
    val receiverTyping = repository.isReceiverIsTyping

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
        repository.sendMessage(contact, message)
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

    companion object : KoinInjector {
        override fun inject(): Module {
            return module {
                viewModel { ChatViewModel(get()) }
            }
        }
    }

}