package com.utsman.chatingan.features.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.features.auth.repository.ProfileRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

class ProfileViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    init {
        profileRepository.initializeUser(viewModelScope)
    }

    val userState = profileRepository.userState

    companion object : KoinInjector {
        override fun inject(): Module {
            return module {
                viewModel { ProfileViewModel(get()) }
            }
        }
    }
}