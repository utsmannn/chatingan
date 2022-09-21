package com.utsman.chatingan.chat.di

import com.utsman.chatingan.chat.repository.ChatRepository
import com.utsman.chatingan.chat.ui.ChatViewModel
import com.utsman.chatingan.common.koin.KoinModule
import org.koin.core.module.Module

object ChatModule : KoinModule {
    override fun modules(): List<Module> {
        return listOf(
            ChatRepository.inject(),
            ChatViewModel.inject()
        )
    }
}