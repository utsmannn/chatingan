package com.utsman.chatingan.home.repository

import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.model.MessageInfo
import org.koin.core.module.Module
import org.koin.dsl.module

interface HomeRepository {
    val userState: FlowEvent<User>
    val contactState: FlowEvent<List<Contact>>
    val tokenState: FlowEvent<String>

    val chatsState: FlowEvent<List<MessageInfo>>

    suspend fun getUser()
    suspend fun getContacts()
    suspend fun getMessages()

    /* Single one shoot */
    //suspend fun getContact(chatInfo: ChatInfo): FlowEvent<Contact>

    companion object : KoinInjector {
        @JvmStatic
        override fun inject(): Module {
            return module {
                single<HomeRepository> { HomeRepositoryImpl(get()) }
            }
        }
    }
}