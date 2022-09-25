package com.utsman.chatingan.home.repository

import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.sdk.data.entity.Chat
import com.utsman.chatingan.sdk.data.entity.ChatInfo
import com.utsman.chatingan.sdk.data.entity.Contact
import org.koin.core.module.Module
import org.koin.dsl.module

interface HomeRepository {
    val userState: FlowEvent<User>
    val contactState: FlowEvent<List<Contact>>
    val tokenState: FlowEvent<String>

    val chatsState: FlowEvent<List<Chat>>

    suspend fun getUser()
    suspend fun getContacts()
    suspend fun getTokenId(id: String)

    suspend fun getChats()

    /* Single one shoot */
    suspend fun getContact(chatInfo: ChatInfo): FlowEvent<Contact>

    companion object : KoinInjector {
        @JvmStatic
        override fun inject(): Module {
            return module {
                single<HomeRepository> { HomeRepositoryImpl(get()) }
            }
        }
    }
}