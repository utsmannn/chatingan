package com.utsman.chatingan.contact.repository

import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.auth.datasources.AuthDataSources
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.StateEvent
import com.utsman.chatingan.common.event.collectToStateEvent
import com.utsman.chatingan.common.event.defaultStateEvent
import com.utsman.chatingan.lib.Chatingan
import com.utsman.chatingan.lib.data.model.Contact
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class ContactRepositoryImpl(
    private val authDataSources: AuthDataSources
) : ContactRepository {

    private val _contacts = defaultStateEvent<List<Contact>>()
    override val contacts: FlowEvent<List<Contact>>
        get() = _contacts

    private val _isContactExist = defaultStateEvent<Boolean>()
    override val isContactExist: FlowEvent<Boolean>
        get() = _isContactExist

    private val _isAddContactSuccess = defaultStateEvent<Boolean>()
    override val isAddContactSuccess: FlowEvent<Boolean>
        get() = _isAddContactSuccess

    private val _user = defaultStateEvent<User>()
    override val user: FlowEvent<User>
        get() = _user

    override suspend fun getContact() {
        Chatingan.getInstance()
            .getAllContact()
            .collectToStateEvent(_contacts)
    }

    override suspend fun getUser() {
        authDataSources.getCurrentUser()
            .collect(_user)
    }

    override suspend fun addContact(contact: Contact) {
        flow {
            emit(
                Chatingan.getInstance()
                    .getChatinganQr()
                    .requestPair(contact)
            )
        }.collectToStateEvent(_isAddContactSuccess)
    }

    override suspend fun checkContactIsExists(contact: Contact) {
        Chatingan.getInstance()
            .getContactByEmail(contact.email)
            .catch {
                _isContactExist.value = StateEvent.Success(false)
            }
            .collect {
                _isContactExist.value = StateEvent.Success(true)
            }
    }
}