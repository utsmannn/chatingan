package com.utsman.chatingan.main.repository

import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.koin.KoinInjector
import org.koin.core.module.Module
import org.koin.dsl.module

interface MainRepository {
    val userState: FlowEvent<User>
    suspend fun checkUser()

    companion object : KoinInjector {
        @JvmStatic
        override fun inject(): Module {
            return module {
                single<MainRepository> { MainRepositoryImpl(get()) }
            }
        }
    }
}