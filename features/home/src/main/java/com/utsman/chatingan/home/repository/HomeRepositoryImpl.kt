package com.utsman.chatingan.home.repository

import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.auth.datasources.AuthDataSources
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.StateEvent
import com.utsman.chatingan.common.event.defaultStateEvent
import com.utsman.chatingan.common.event.errorStateEvent
import com.utsman.chatingan.common.event.filterFlow
import com.utsman.chatingan.common.event.invoke
import com.utsman.chatingan.common.event.loadingEventValue
import com.utsman.chatingan.common.event.onSuccess
import com.utsman.chatingan.sdk.Chatingan
import com.utsman.chatingan.sdk.data.entity.Chat
import com.utsman.chatingan.sdk.data.entity.ChatInfo
import com.utsman.chatingan.sdk.data.entity.Contact
import kotlinx.coroutines.flow.map

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
            _userState.value = it
        }
    }

    override suspend fun getContacts() {
        _contactState.value = loadingEventValue()
        Chatingan
            .getInstance()
            .getContacts().collect(_contactState)
    }

    override suspend fun getChats() {
        _chatsState.value = loadingEventValue()
        Chatingan
            .getInstance()
            .getChats()
            .collect(_chatsState)
    }

    override suspend fun getContact(chatInfo: ChatInfo): FlowEvent<Contact> {
        val contactMeId = Chatingan.getInstance().config.contact.id
        val contactId = chatInfo.memberIds.firstOrNull { it != contactMeId }
            ?: return errorStateEvent("Contact Id is null")
        return Chatingan.getInstance().getContact(contactId)
    }
}