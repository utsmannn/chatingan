package com.utsman.chatingan.contact.repository

import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.common.base.BaseRepository
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.lib.data.model.Contact
import kotlinx.coroutines.CoroutineScope
import org.koin.core.module.Module
import org.koin.dsl.module

interface ContactRepository {
    fun getContacts(coroutineScope: CoroutineScope): FlowEvent<List<Contact>>
    fun getUser(coroutineScope: CoroutineScope): FlowEvent<User>

    fun addContact(coroutineScope: CoroutineScope, contact: Contact): FlowEvent<Boolean>
    fun checkContactIsExists(coroutineScope: CoroutineScope, contact: Contact): FlowEvent<Boolean>

    companion object : KoinInjector {
        override fun inject(): Module {
            return module {
                single<ContactRepository> { ContactRepositoryImpl(get()) }
            }
        }
    }
}