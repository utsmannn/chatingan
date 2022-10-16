package com.utsman.chatingan.auth

import com.utsman.chatingan.auth.data.AuthConfig
import com.utsman.chatingan.auth.datasources.AuthDataSources
import com.utsman.chatingan.common.koin.KoinModule
import org.koin.core.module.Module

object AuthModule : KoinModule {
    override fun modules(): List<Module> {
        return listOf(
            AuthConfig.inject(),
            AuthDataSources.inject()
        )
    }
}