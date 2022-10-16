package com.utsman.chatingan.home

import com.utsman.chatingan.common.koin.KoinModule
import com.utsman.chatingan.home.repository.HomeRepository
import com.utsman.chatingan.home.ui.HomeViewModel
import org.koin.core.module.Module

object HomeModule : KoinModule {

    override fun modules(): List<Module> {
        return listOf(
            HomeRepository.inject(),
            HomeViewModel.inject()
        )
    }
}