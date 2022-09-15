package com.utsman.chatingan.features.auth.repository

import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.common.event.StateEvent
import com.utsman.chatingan.common.koin.KoinInjector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.module.Module
import org.koin.dsl.module

interface ProfileRepository {
    val userState: StateFlow<StateEvent<User>>
    fun initializeUser(scope: CoroutineScope)

    companion object : KoinInjector {
        override fun inject(): Module {
            return module {
                single<ProfileRepository> { ProfileRepositoryImpl() }
            }
        }
    }
}