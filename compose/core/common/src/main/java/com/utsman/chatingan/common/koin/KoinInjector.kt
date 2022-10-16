package com.utsman.chatingan.common.koin

import org.koin.core.module.Module

interface KoinInjector {
    fun inject(): Module
}