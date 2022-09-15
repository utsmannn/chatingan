package com.utsman.chatingan.common.koin

import org.koin.core.KoinApplication
import org.koin.core.module.Module

fun KoinApplication.moduleListOf(vararg modulesList: List<Module>): KoinApplication {
    return modules(modulesList.flatMap { it })
}