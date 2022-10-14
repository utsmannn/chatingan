package com.utsman.chatingan.chat.ui.chat

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.utsman.chatingan.chat.repository.CameraRepository
import com.utsman.chatingan.chat.repository.ChatRepository
import com.utsman.chatingan.chat.routes.ChatRoute
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.model.Message
import com.utsman.chatingan.navigation.RouteViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

class ChatViewModel(
    private val repository: ChatRepository,
    private val cameraRepository: CameraRepository
) : RouteViewModel(ChatRoute.Chat) {
    private val rawText = MutableStateFlow("")
    val textState = rawText
    val imageFileState = cameraRepository.imageFileState

    private val _pagingData: MutableStateFlow<PagingData<Message>> = MutableStateFlow(PagingData.empty())
    val pagingData: Flow<PagingData<Message>>
        get() = _pagingData

    fun getMessages(contact: Contact) = viewModelScope.launch(Dispatchers.IO) {
        repository.getMessages(this, contact)
            .collect(_pagingData)
    }

    fun getTyping(contactId: String): Flow<Boolean> {
        return repository.getContact(contactId).map { it.isTyping }
    }

    fun sendMessage(contact: Contact, message: Message) =
        repository.sendMessage(viewModelScope, contact, message)

    fun markAsRead(contact: Contact, message: Message) =
        repository.markAsRead(viewModelScope, contact, message)

    fun listenForTyping(contact: Contact) = viewModelScope.launch {
        rawText
            .filter { it.length > 2 }
            .collect {
                repository.setTypingStatus(viewModelScope, contact, true)
                delay(1000)
                repository.setTypingStatus(viewModelScope, contact, false)
            }
    }

    fun setText(text: String) = viewModelScope.launch {
        rawText.value = text
    }

    override fun dispose() {
        viewModelScope.launch {
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