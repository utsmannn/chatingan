package com.utsman.chatingan.features.auth.di

import com.utsman.chatingan.common.koin.KoinModule
import com.utsman.chatingan.features.auth.repository.AuthRepository
import com.utsman.chatingan.features.auth.repository.ProfileRepository
import com.utsman.chatingan.features.auth.ui.LoginViewModel
import com.utsman.chatingan.features.auth.ui.ProfileViewModel
import org.koin.core.module.Module

object AuthModule : KoinModule {
    override fun modules(): List<Module> {
        return listOf(
            AuthRepository.inject(),
            ProfileRepository.inject(),
            LoginViewModel.inject(),
            ProfileViewModel.inject()
        )
    }
}