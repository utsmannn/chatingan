package com.utsman.chatingan.common.koin

import org.koin.core.module.Module

interface KoinModule {
    fun modules(): List<Module>
}