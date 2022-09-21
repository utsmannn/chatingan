package com.utsman.chatingan.contact.repository

import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.sdk.data.entity.Contact
import org.koin.core.module.Module
import org.koin.dsl.module

interface ContactRepository {
    val user: FlowEvent<User>
    val contacts: FlowEvent<List<Contact>>

    suspend fun getContact()
    suspend fun getUser()

    companion object : KoinInjector {
        override fun inject(): Module {
            return module {
                single<ContactRepository> { ContactRepositoryImpl(get()) }
            }
        }
    }
}