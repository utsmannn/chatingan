package com.utsman.chatingan.chat.ui

import androidx.lifecycle.viewModelScope
import com.utsman.chatingan.chat.repository.ChatRepository
import com.utsman.chatingan.chat.routes.ChatRoute
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.navigation.RouteViewModel
import com.utsman.chatingan.sdk.data.entity.ChatInfo
import com.utsman.chatingan.sdk.data.entity.Contact
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

class ChatViewModel(
    private val repository: ChatRepository
) : RouteViewModel(ChatRoute.Chat) {
    val chatState = repository.chatState
    fun getChat(contact: Contact) = viewModelScope.launch {
        repository.getChat(contact)
    }

    fun readChat(contact: Contact) = viewModelScope.launch {
        repository.readChat(contact)
    }

    fun sendMessage(contact: Contact, message: String, chatInfo: ChatInfo?) = viewModelScope.launch {
        repository.sendMessage(contact, message, chatInfo)
    }

    companion object : KoinInjector {
        override fun inject(): Module {
            return module {
                viewModel { ChatViewModel(get()) }
            }
        }
    }

}