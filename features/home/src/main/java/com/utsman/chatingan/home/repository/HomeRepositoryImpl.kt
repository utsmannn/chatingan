package com.utsman.chatingan.home.repository

import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.auth.datasources.AuthDataSources
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.defaultStateEvent
import com.utsman.chatingan.common.event.filterFlow
import com.utsman.chatingan.common.event.onSuccess
import com.utsman.chatingan.sdk.Chatingan
import com.utsman.chatingan.sdk.data.entity.Chat
import com.utsman.chatingan.sdk.data.entity.Contact

class HomeRepositoryImpl(
    private val authDataSources: AuthDataSources
) : HomeRepository {

    private val _userState = defaultStateEvent<User>()
    override val userState: FlowEvent<User>
        get() = _userState

    private val _contactState = defaultStateEvent<List<Contact>>()
    override val contactState: FlowEvent<List<Contact>>
        get() = _contactState

    private val _tokenState = defaultStateEvent<String>()
    override val tokenState: FlowEvent<String>
        get() = _tokenState

    private val _chatsState = defaultStateEvent<List<Chat>>()
    override val chatsState: FlowEvent<List<Chat>>
        get() = _chatsState

    override suspend fun getUser() {
        authDataSources.getCurrentUser().collect {
            it.onSuccess { user ->
                val contact = Contact(
                    id = user.id,
                    name = user.name,
                    image = user.photoUrl
                )

                println("ASUUUU try add contact")
                Chatingan
                    .getInstance()
                    .addMeContact(contact)
            }
            _userState.value = it
        }
    }

    override suspend fun getContacts() {
        Chatingan
            .getInstance()
            .contacts().collect(_contactState)
    }

    override suspend fun getTokenId(id: String) {
        Chatingan
            .getInstance()
            .tokenForId(id).collect(_tokenState)
    }

    override suspend fun getChats() {
        println("ASUUUU -> get chats.....")
        Chatingan
            .getInstance()
            .getChats()
            .filterFlow {
                it.id.isNotEmpty()
            }
            .collect(_chatsState)
    }
}