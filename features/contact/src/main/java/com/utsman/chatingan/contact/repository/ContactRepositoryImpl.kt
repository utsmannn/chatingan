package com.utsman.chatingan.contact.repository

import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.auth.datasources.AuthDataSources
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.defaultStateEvent
import com.utsman.chatingan.sdk.Chatingan
import com.utsman.chatingan.sdk.data.entity.Contact
import kotlinx.coroutines.flow.distinctUntilChangedBy

class ContactRepositoryImpl(
    private val authDataSources: AuthDataSources
) : ContactRepository {

    private val _contacts = defaultStateEvent<List<Contact>>()
    override val contacts: FlowEvent<List<Contact>>
        get() = _contacts

    private val _user = defaultStateEvent<User>()
    override val user: FlowEvent<User>
        get() = _user

    override suspend fun getContact() {
        Chatingan
            .getInstance()
            .contacts()
            .distinctUntilChangedBy { it }
            .collect(_contacts)
    }

    override suspend fun getUser() {
        authDataSources.getCurrentUser()
            .collect(_user)
    }
}