package com.utsman.chatingan.login.ui

import androidx.lifecycle.viewModelScope
import com.utsman.chatingan.auth.component.AuthComponent
import com.utsman.chatingan.navigation.RouteViewModel
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.login.repository.LoginRepository
import com.utsman.chatingan.routes.AppRoute
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

class LoginViewModel(
    private val loginRepository: LoginRepository
) : RouteViewModel(AppRoute.Login) {

    val signInState = loginRepository.signInState

    fun signIn(authComponent: AuthComponent) {
        viewModelScope.launch {
            loginRepository.signIn(authComponent)
        }
    }

    companion object : KoinInjector {
        @JvmStatic
        override fun inject(): Module {
            return module {
                viewModel { LoginViewModel(get()) }
            }
        }
    }
}