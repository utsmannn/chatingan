package com.utsman.chatingan.contact.ui

import androidx.lifecycle.viewModelScope
import com.utsman.chatingan.navigation.RouteViewModel
import com.utsman.chatingan.common.koin.KoinInjector
import com.utsman.chatingan.contact.routes.ContactRoute
import com.utsman.chatingan.contact.repository.ContactRepository
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

class ContactViewModel(
    private val contactRepository: ContactRepository
) : RouteViewModel(ContactRoute.Contact) {
    val contactState = contactRepository.contacts
    val userState = contactRepository.user

    init {
        viewModelScope.launch { contactRepository.getContact() }
        viewModelScope.launch { contactRepository.getUser() }
    }

    companion object : KoinInjector {
        override fun inject(): Module {
            return module {
                viewModel { ContactViewModel(get()) }
            }
        }
    }
}