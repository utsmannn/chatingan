package com.utsman.chatingan.home.repository

import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.auth.datasources.AuthDataSources
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.collectToStateEvent
import com.utsman.chatingan.common.event.defaultStateEvent
import com.utsman.chatingan.lib.Chatingan
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.lib.data.model.MessageInfo

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

    private val _chatsState = defaultStateEvent<List<MessageInfo>>()
    override val chatsState: FlowEvent<List<MessageInfo>>
        get() = _chatsState

    override suspend fun getUser() {
        authDataSources.getCurrentUser().collect {
            _userState.value = it
        }
    }

    override suspend fun getContacts() {
        Chatingan
            .getInstance()
            .getAllContact()
            .collectToStateEvent(_contactState)
    }

    override suspend fun getChats() {
        Chatingan
            .getInstance()
            .getMessagesInfo()
            .collectToStateEvent(_chatsState)
    }
}