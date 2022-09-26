package com.utsman.chatingan.sdk.database

import com.utsman.chatingan.sdk.data.entity.Contact
import com.utsman.chatingan.sdk.data.store.ContactStore

class ContactDatabase : RemoteDatabase<ContactStore, Contact>(ContactStore::class) {

    override fun path(): String {
        return COLLECTION_CONTACT
    }

    override fun dataMapper(store: ContactStore): Contact {
        return store.toContact()
    }

    override fun dateField(): String {
        return FIELD_LAST_UPDATE
    }

    companion object {
        internal const val COLLECTION_CONTACT = "contacts"
    }
}