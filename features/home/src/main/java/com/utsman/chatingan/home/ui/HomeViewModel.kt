package com.utsman.chatingan.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.home.repository.HomeRepository
import com.utsman.chatingan.lib.Chatingan
import com.utsman.chatingan.lib.data.model.Message
import com.utsman.chatingan.lib.data.model.MessageInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

class HomeViewModel(
    private val homeRepository: HomeRepository
) : ViewModel() {
    val userState = homeRepository.userState
    val contactState = homeRepository.contactState
    val tokenState = homeRepository.tokenState

    val lastMessageFlow: MutableStateFlow<Message.TextMessages> = MutableStateFlow(Message.emptyTextMessage())

    val chatState = homeRepository.chatsState

    init {
        viewModelScope.launch {
            homeRepository.getMessages()
        }
    }

    fun getUser() = viewModelScope.launch {
        homeRepository.getUser()
    }

    fun token(id: String) = viewModelScope.launch {
        //homeRepository.getTokenId(id)
    }

    suspend fun getLastMessage(messageInfo: MessageInfo): StateFlow<Message> {
        //return Chatingan.getInstance().getLastMessage(messageInfo)
        return withContext(viewModelScope.coroutineContext) {
            Chatingan.getInstance().getLastMessage(messageInfo).stateIn(viewModelScope)
        }
    }

    companion object : KoinInjector {
        @JvmStatic
        override fun inject(): Module {
            return module {
                viewModel { HomeViewModel(get()) }
            }
        }
    }
}