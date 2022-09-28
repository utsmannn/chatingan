package com.utsman.chatingan.chat.ui

import androidx.lifecycle.viewModelScope
import com.utsman.chatingan.chat.repository.ChatRepository
import com.utsman.chatingan.chat.routes.ChatRoute
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.navigation.RouteViewModel
import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.entity.MessageChat
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
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
    val chatState = repository.chatState
    private val rawText = MutableStateFlow("")
    private val isTyping = MutableStateFlow(false)
    val textState = rawText

    fun getChat(contact: Contact) = viewModelScope.launch {
        repository.getChat(contact)
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

    fun setTypingStatus(contact: Contact, isTyping: Boolean) = viewModelScope.launch {
        repository.setTypingStatus(contact, isTyping)
    }

    fun listenForTyping(contact: Contact)  {
        /*viewModelScope.launch {
            var resultFor = ""
            rawText
                .debounce(500)
                .collect { resultText ->
                    *//*if (isHasSend) {
                        println("ASUUUUUUUUU -> ${it.isNotEmpty()} | $it")
                    }*//*
                    //if (resultText == resultFor) return@collect

                    *//*delay(500)
                    if (resultText != resultFor) {
                        //println("ASUUUUUU -> typing")
                        isTyping.value = !isTyping.value
                        resultFor = resultText
                    }

                    delay(500)
                    if (resultFor == resultText) {
                        //println("ASUUUU stop")
                        isTyping.value = !isTyping.value
                    }*//*
                    if (resultText != resultFor) {
                        //println("ASUUUUUU -> typing")
                        //isTyping.value = !isTyping.value
                        resultFor = resultText
                    }

                    isTyping.value = resultFor != resultText
                }
        }*/

        viewModelScope.launch {
            rawText
                .debounce(100)
                .distinctUntilChangedBy { it }
                .collect {
                    println("ASUUUU -> $it")

                    delay(1000)
                    println("ASUUUUUU stop")
                }
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