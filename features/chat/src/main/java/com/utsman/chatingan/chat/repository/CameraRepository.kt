package com.utsman.chatingan.chat.repository

import com.utsman.chatingan.common.koin.KoinInjector
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

interface CameraRepository {
    val imageFileState: StateFlow<Result<File>>

    suspend fun useFile(file: File)
    suspend fun clearFile()

    companion object : KoinInjector {
        override fun inject(): Module {
            return module {
                single<CameraRepository> { CameraRepositoryImpl() }
            }
        }
    }
}