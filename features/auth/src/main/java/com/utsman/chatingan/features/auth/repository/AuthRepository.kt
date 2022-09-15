package com.utsman.chatingan.features.auth.repository

import com.utsman.chatingan.auth.AuthComponent
import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.common.event.StateEvent
import com.utsman.chatingan.common.koin.KoinInjector
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.module.Module
import org.koin.dsl.module

interface AuthRepository {
    val isHasSignIn: StateFlow<Boolean>

    val signInState: StateFlow<StateEvent<User>>
    val signOutState: StateFlow<StateEvent<Nothing>>

    suspend fun signIn(component: AuthComponent)
    suspend fun signOut()

    companion object : KoinInjector {
        override fun inject(): Module {
            return module {
                single<AuthRepository> { AuthRepositoryImpl(get()) }
            }
        }
    }
}