package com.utsman.chatingan.features.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utsman.chatingan.auth.AuthComponent
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.features.auth.repository.AuthRepository
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    val signInState = authRepository.signInState
    val signOutState = authRepository.signOutState

    fun signIn(authComponent: AuthComponent) = viewModelScope.launch {
        authRepository.signIn(authComponent)
    }

    fun signOut() = viewModelScope.launch {
        authRepository.signOut()
    }

    companion object : KoinInjector {
        override fun inject(): Module {
            return module {
                viewModel { LoginViewModel(get()) }
            }
        }
    }
}