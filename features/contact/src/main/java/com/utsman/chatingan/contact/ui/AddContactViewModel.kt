package com.utsman.chatingan.contact.ui

import androidx.compose.runtime.remember
import androidx.lifecycle.viewModelScope
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.contact.repository.ContactRepository
import com.utsman.chatingan.contact.routes.ContactRoute
import com.utsman.chatingan.lib.data.model.Contact
import com.utsman.chatingan.navigation.RouteViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

class AddContactViewModel(
    private val contactRepository: ContactRepository
) : RouteViewModel(ContactRoute.AddContact) {

    val isAddContactSuccess = contactRepository.isAddContactSuccess
    val isContactExists = contactRepository.isContactExist

    fun addContact(contact: Contact) = viewModelScope.launch {
        contactRepository.addContact(contact)
    }

    fun checkContactIsExist(contact: Contact) = viewModelScope.launch {
        contactRepository.checkContactIsExists(contact)
    }

    companion object : KoinInjector {
        override fun inject(): Module {
            return module {
                viewModel { AddContactViewModel(get()) }
            }
        }
    }
}