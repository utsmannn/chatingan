package com.utsman.chatingan.contact.repository

import com.utsman.chatingan.auth.data.User
import com.utsman.chatingan.auth.datasources.AuthDataSources
import com.utsman.chatingan.common.event.FlowEvent
import com.utsman.chatingan.common.event.StateEvent
import com.utsman.chatingan.common.event.collectToStateEvent
import com.utsman.chatingan.common.event.defaultStateEvent
import com.utsman.chatingan.lib.Chatingan
import com.utsman.chatingan.lib.data.model.Contact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class ContactRepositoryImpl(
    private val authDataSources: AuthDataSources
) : ContactRepository {

    override fun getContacts(coroutineScope: CoroutineScope): FlowEvent<List<Contact>> {
        val contacts = defaultStateEvent<List<Contact>>()
        coroutineScope.launch {
            Chatingan.getInstance()
                .getAllContact()
                .collectToStateEvent(contacts)
        }
        return contacts
    }

    override fun getUser(coroutineScope: CoroutineScope): FlowEvent<User> {
        val user = defaultStateEvent<User>()
        coroutineScope.launch {
            authDataSources.getCurrentUser()
                .collect(user)
        }

        return user
    }

    override fun addContact(coroutineScope: CoroutineScope, contact: Contact): FlowEvent<Boolean> {
        val isAddContactSuccess = defaultStateEvent<Boolean>()
        coroutineScope.launch {
            flow {
                emit(
                    Chatingan.getInstance()
                        .getChatinganQr()
                        .requestPair(contact)
                )
            }.collectToStateEvent(isAddContactSuccess)
        }

        return isAddContactSuccess
    }

    override fun checkContactIsExists(
        coroutineScope: CoroutineScope,
        contact: Contact
    ): FlowEvent<Boolean> {
        val isContactExist = defaultStateEvent<Boolean>()
        coroutineScope.launch {
            Chatingan.getInstance()
                .getContactByEmail(contact.email)
                .catch {
                    isContactExist.value = StateEvent.Success(false)
                }
                .collect {
                    isContactExist.value = StateEvent.Success(true)
                }
        }
        return isContactExist
    }
}