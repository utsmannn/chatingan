package com.utsman.chatingan.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.home.repository.HomeRepository
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

class HomeViewModel(
    private val homeRepository: HomeRepository
) : ViewModel() {
    val userState = homeRepository.userState
    val contactState = homeRepository.contactState
    val tokenState = homeRepository.tokenState

    val chatState = homeRepository.chatsState

    init {
        viewModelScope.launch {
            //homeRepository.getContacts()
            homeRepository.getChats()
        }
    }

    fun getUser() = viewModelScope.launch {
        homeRepository.getUser()
    }

    fun token(id: String) = viewModelScope.launch {
        //homeRepository.getTokenId(id)
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