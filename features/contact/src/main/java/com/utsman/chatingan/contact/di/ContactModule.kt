package com.utsman.chatingan.contact.di

import com.utsman.chatingan.common.koin.KoinModule
import com.utsman.chatingan.contact.repository.ContactRepository
import com.utsman.chatingan.contact.ui.ContactViewModel
import org.koin.core.module.Module

object ContactModule : KoinModule {
    override fun modules(): List<Module> {
        return listOf(
            ContactRepository.inject(),
            ContactViewModel.inject()
        )
    }
}