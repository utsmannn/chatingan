package com.utsman.chatingan.home.repository

import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.koin.KoinInjector
import org.koin.core.module.Module
import org.koin.dsl.module

interface HomeRepository {
    val userState: FlowEvent<User>

    suspend fun getUser()

    companion object : KoinInjector {
        @JvmStatic
        override fun inject(): Module {
            return module {
                single<HomeRepository> { HomeRepositoryImpl(get()) }
            }
        }
    }
}