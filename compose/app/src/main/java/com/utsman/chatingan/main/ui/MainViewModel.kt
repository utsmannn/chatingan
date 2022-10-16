package com.utsman.chatingan.main.ui

import androidx.lifecycle.viewModelScope
import com.utsman.chatingan.navigation.RouteViewModel
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.main.repository.MainRepository
import com.utsman.chatingan.routes.AppRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

class MainViewModel(
    private val repository: MainRepository
) : RouteViewModel(AppRoute.Splash) {
    val userState = repository.userState
    val firebaseTokenState = repository.firebaseTokenState

    init {
        viewModelScope.launch {
            repository.checkUser()
        }
        viewModelScope.launch {
            repository.getFirebaseToken()
        }
    }

    companion object : KoinInjector {
        @JvmStatic
        override fun inject(): Module {
            return module {
                viewModel { MainViewModel(get()) }
            }
        }
    }
}