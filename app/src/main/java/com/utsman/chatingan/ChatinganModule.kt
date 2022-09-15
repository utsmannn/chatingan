package com.utsman.chatingan

import com.utsman.chatingan.common.koin.KoinModule
import com.utsman.chatingan.login.repository.LoginRepository
import com.utsman.chatingan.login.ui.LoginViewModel
import com.utsman.chatingan.main.repository.MainRepository
import com.utsman.chatingan.main.ui.MainViewModel
import org.koin.core.module.Module

object ChatinganModule : KoinModule {
    override fun modules(): List<Module> {
        return listOf(
            MainRepository.inject(),
            MainViewModel.inject(),

            LoginRepository.inject(),
            LoginViewModel.inject()
        )
    }
}