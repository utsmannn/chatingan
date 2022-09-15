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

    fun getUser() = viewModelScope.launch {
        homeRepository.getUser()
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