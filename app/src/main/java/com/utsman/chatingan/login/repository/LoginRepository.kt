package com.utsman.chatingan.login.repository

import com.utsman.chatingan.auth.AuthComponent
import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.koin.KoinInjector
import kotlinx.coroutines.CoroutineScope
import org.koin.core.module.Module
import org.koin.dsl.module

interface LoginRepository {
    val signInState: FlowEvent<User>

    suspend fun signIn(authComponent: AuthComponent)

    companion object : KoinInjector {
        @JvmStatic
        override fun inject(): Module {
            return module {
                single<LoginRepository> { LoginRepositoryImpl(get()) }
            }
        }
    }
}