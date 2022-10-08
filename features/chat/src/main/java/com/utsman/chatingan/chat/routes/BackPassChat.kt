package com.utsman.chatingan.chat.routes

import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.navigation.BackPassHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.module.Module
import org.koin.dsl.module

class BackPassChat : BackPassHelper {

    private val _currentBack = MutableStateFlow("")
    override val currentBack: StateFlow<String>
        get() = _currentBack

    override fun setBackFrom(screenName: String) {
        _currentBack.value = screenName
    }

    override fun clearBackFrom() {
        _currentBack.value = ""
    }

    companion object : KoinInjector {
        override fun inject(): Module {
            return module {
                single { BackPassChat() }
            }
        }

    }
}