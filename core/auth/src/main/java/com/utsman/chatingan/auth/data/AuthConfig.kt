package com.utsman.chatingan.auth.data

import com.utsman.chatingan.common.koin.KoinInjector
import org.koin.core.module.Module
import org.koin.dsl.module

class AuthConfig {
    var clientId: String = ""

    companion object : KoinInjector {
        override fun inject(): Module {
            return module {
                single { AuthConfig() }
            }
        }

    }
}