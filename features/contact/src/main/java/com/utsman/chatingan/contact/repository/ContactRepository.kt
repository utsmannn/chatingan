package com.utsman.chatingan.contact.repository

import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.lib.data.model.Contact
import org.koin.core.module.Module
import org.koin.dsl.module

interface ContactRepository {
    val user: FlowEvent<User>
    val contacts: FlowEvent<List<Contact>>

    val isContactExist: FlowEvent<Boolean>
    val isAddContactSuccess: FlowEvent<Boolean>

    suspend fun getContact()
    suspend fun getUser()

    suspend fun addContact(contact: Contact)
    suspend fun checkContactIsExists(contact: Contact)

    companion object : KoinInjector {
        override fun inject(): Module {
            return module {
                single<ContactRepository> { ContactRepositoryImpl(get()) }
            }
        }
    }
}