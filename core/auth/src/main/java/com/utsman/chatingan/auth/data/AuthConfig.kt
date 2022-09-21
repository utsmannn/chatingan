package com.utsman.chatingan.auth.data

import com.utsman.chatingan.common.koin.KoinInjector
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.module.Module
import org.koin.dsl.module

class AuthConfig {
    var clientId: String = ""

    companion object : KoinInjector, KoinComponent {
        override fun inject(): Module {
            return module {
                single { AuthConfig() }
            }
        }

        fun setClientId(clientId: String) {
            val authConfig: AuthConfig = get()
            authConfig.clientId = clientId
        }
    }
}